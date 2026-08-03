/*
 * SPORT TV — сервер сайта: статика + API-прокси к sport-tv.by.
 * Прокси нужен, т.к. list.php (расписание по дням) и HTML-страницы
 * оригинала не отдают CORS-заголовки. Запуск: node serve.js [port]
 */
'use strict';
const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const { ActivationSessionStore } = require('./activation-session-store');

const ROOT = __dirname;
const PORT = Number(process.argv[2]) || 4173;
const ORIGIN = 'https://sport-tv.by';
const UA = 'SportTvWebsite/1.0 (+redesign)';
const CACHE_TTL_MS = 60_000;
const ACTIVATION_PUBLIC_ORIGIN = process.env.ACTIVATION_PUBLIC_ORIGIN || '';
const activationSessions = new ActivationSessionStore();

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.ico': 'image/x-icon',
  '.json': 'application/json',
};

/* ---------- upstream fetch с минутным кэшем ---------- */
const cache = new Map();

function upstream(urlPath) {
  const hit = cache.get(urlPath);
  if (hit && Date.now() - hit.at < CACHE_TTL_MS) return Promise.resolve(hit.body);

  return new Promise((resolve, reject) => {
    https.get(ORIGIN + urlPath, { headers: { 'User-Agent': UA, Accept: '*/*' } }, (res) => {
      if (res.statusCode !== 200) {
        res.resume();
        reject(new Error(`upstream HTTP ${res.statusCode}`));
        return;
      }
      const chunks = [];
      res.on('data', (c) => chunks.push(c));
      res.on('end', () => {
        const body = Buffer.concat(chunks).toString('utf-8');
        cache.set(urlPath, { at: Date.now(), body });
        resolve(body);
      });
    }).on('error', reject);
  });
}

/* ---------- парсеры HTML оригинала ---------- */
function absolutize(html) {
  return html
    .replace(/(src|href)="\/(?!\/)/g, `$1="${ORIGIN}/`)
    .replace(/href="https:\/\/sport-tv\.by\/(news\/[^"]+)"/g, 'href="#/article?path=/$1"');
}

function stripDangerous(html) {
  return html
    .replace(/<script[\s\S]*?<\/script>/gi, '')
    .replace(/<iframe[\s\S]*?<\/iframe>/gi, '')
    .replace(/\son\w+="[^"]*"/gi, '');
}

/** Карточки новостей: <a class="event" href=...><img...><span class="content">... */
function parseNewsList(html) {
  const items = [];
  const re = /<a class="event" href="([^"]+)">\s*<img src="([^"]*)"[^>]*>[\s\S]*?<span class="top">([^<]*)<\/span>[\s\S]*?<span class="category">([^<]*)<\/span>\s*<span class="title">([^<]*)<\/span>/g;
  let m;
  while ((m = re.exec(html)) !== null) {
    items.push({
      path: m[1],
      image: m[2] ? (m[2].startsWith('http') ? m[2] : ORIGIN + m[2]) : '',
      date: m[3].trim(),
      category: m[4].trim(),
      title: m[5].trim(),
    });
  }
  const pages = [...html.matchAll(/\?PAGEN_1=(\d+)/g)]
    .reduce((max, p) => Math.max(max, Number(p[1])), 1);
  return { items, pages };
}

/**
 * Расписание /schedule/: верхний блок — незавершённые трансляции сегодня,
 * .shedule_week — вся неделя, дата дня приходит как <h3>6 июля 2026</h3>
 * внутри первого item дня. Времена — минские (UTC+3, без DST).
 */
const MONTHS_RU = {
  'января': '01', 'февраля': '02', 'марта': '03', 'апреля': '04',
  'мая': '05', 'июня': '06', 'июля': '07', 'августа': '08',
  'сентября': '09', 'октября': '10', 'ноября': '11', 'декабря': '12',
};

function parseScheduleWeek(html) {
  const hdr = /<div class="date">[^<]*?(\d{2})\.(\d{2})\.(\d{4})/.exec(html);
  const todayKey = hdr ? `${hdr[3]}-${hdr[2]}-${hdr[1]}` : null;

  const weekIdx = html.indexOf('shedule_week');
  const topHtml = weekIdx > 0 ? html.slice(0, weekIdx) : '';
  const weekHtml = weekIdx > 0 ? html.slice(weekIdx) : html;

  function collect(section, startKey) {
    const re = /<div class="item">\s*(?:<h3>([^<]*)<\/h3>\s*)?<a href="([^"]+)">([^<]+)<\/a>\s*<img src="([^"]*)"[^>]*>\s*<a[^>]*>([\s\S]*?)<\/a>/g;
    const out = [];
    let m;
    let curKey = startKey;
    while ((m = re.exec(section)) !== null) {
      const [, h3, path, time, img, rawTitle] = m;
      if (h3) {
        const dm = /(\d{1,2})\s+([а-яё]+)\s+(\d{4})/i.exec(h3.trim());
        if (dm && MONTHS_RU[dm[2].toLowerCase()]) {
          curKey = `${dm[3]}-${MONTHS_RU[dm[2].toLowerCase()]}-${String(dm[1]).padStart(2, '0')}`;
        }
      }
      if (!curKey) continue;
      out.push({
        day: curKey,
        time: time.trim(),
        title: rawTitle.replace(/<[^>]+>/g, '').trim(),
        path,
        image: img ? (img.startsWith('http') ? img : ORIGIN + img) : '',
        startMs: Date.parse(`${curKey}T${time.trim()}:00+03:00`),
      });
    }
    return out;
  }

  const week = [];
  for (const item of collect(weekHtml, null)) {
    let bucket = week.find((d) => d.date === item.day);
    if (!bucket) {
      bucket = { date: item.day, items: [] };
      week.push(bucket);
    }
    bucket.items.push(item);
  }
  week.forEach((d) => d.items.sort((a, b) => a.startMs - b.startMs));

  return { today: collect(topHtml, todayKey), todayDate: todayKey, week };
}

/**
 * Страница трансляции: заголовок + конфиг Playerjs.
 * file — YouTube-ссылка у ретрансляций, m3u8 у собственных эфиров,
 * пусто — эфир ещё не начался.
 */
function parseBroadcast(html) {
  const h1 = /<h1[^>]*>([\s\S]*?)<\/h1>/.exec(html);
  const cfg = /Playerjs\(\{([\s\S]*?)\}\)/.exec(html);
  let file = null;
  let poster = null;
  if (cfg) {
    const f = /file:\s*"([^"]*)"/.exec(cfg[1]);
    file = f && f[1] ? f[1] : null;
    const po = /poster:\s*"([^"]*)"/.exec(cfg[1]);
    poster = po && po[1] ? (po[1].startsWith('http') ? po[1] : ORIGIN + po[1]) : null;
  }
  const dt = /<div class="date">([^<]*)<\/div>/.exec(html);
  return {
    title: h1 ? h1[1].replace(/<[^>]+>/g, '').trim() : '',
    file,
    poster,
    date: dt ? dt[1].trim() : '',
  };
}

/** Контент статьи/страницы: <h1> + блок .left (статья) или .article (инфо-страница). */
function parsePage(html) {
  const h1 = /<h1>([\s\S]*?)<\/h1>/.exec(html);
  const title = h1 ? h1[1].replace(/<[^>]+>/g, '').trim() : '';

  let body = '';
  const left = /<div class="left">([\s\S]*?)<div class="banners">/.exec(html);
  const article = /<div class="article">([\s\S]*?)<div class="banners">/.exec(html);
  if (article) body = article[1];
  else if (left) body = left[1];
  body = body.replace(/<\/div>\s*$/, '');

  return { title, html: absolutize(stripDangerous(body)) };
}

/* ---------- API ---------- */
const NEWS_CATS = new Set(['futbol', 'hokkey', 'gandbol', 'basketbol', 'volleyball', 'drugoy-sport']);
const PAGE_WHITELIST = new Set(['/oferta/', '/payment/', '/informatsiya-dlya-pravoobladateley/']);

async function handleApi(req, res, urlObj) {
  const send = (code, data) => {
    res.writeHead(code, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store',
      'X-Content-Type-Options': 'nosniff',
    });
    res.end(JSON.stringify(data));
  };

  try {
    const p = urlObj.pathname;

    if (p === '/api/create-activation-session.php' && req.method === 'POST') {
      const body = await readJsonBody(req);
      if (!body.device_id || typeof body.device_id !== 'string') {
        return send(400, { error: 'device_id is required' });
      }
      const publicOrigin = ACTIVATION_PUBLIC_ORIGIN || `${urlObj.protocol}//${urlObj.host}`;
      return send(201, activationSessions.createSession(body.device_id, publicOrigin, body.plan_id || null));
    }

    if (p === '/api/check-activation-session.php' && req.method === 'GET') {
      const sessionId = urlObj.searchParams.get('sessionId') || '';
      const status = activationSessions.getStatus(sessionId);
      return status ? send(200, { status }) : send(404, { error: 'session not found' });
    }

    if (p === '/api/activation-session.php' && req.method === 'GET') {
      const sessionId = urlObj.searchParams.get('session') || '';
      const session = activationSessions.getPublicSession(sessionId);
      return session ? send(200, session) : send(404, { error: 'session not found' });
    }

    if (p === '/api/activate-session.php' && req.method === 'POST') {
      const body = await readJsonBody(req);
      const currentStatus = activationSessions.getStatus(body.session || '');
      if (!currentStatus) return send(404, { error: 'session not found' });
      if (currentStatus === 'expired') return send(410, { status: currentStatus });
      if (typeof body.email !== 'string' || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(body.email.trim())) {
        return send(400, { error: 'valid email is required' });
      }
      const status = activationSessions.activateSession(body.session || '');
      return send(200, { status });
    }

    if (p === '/api/check-subscription.php' && req.method === 'GET') {
      const deviceId = urlObj.searchParams.get('device_id') || '';
      if (!deviceId) return send(400, { error: 'device_id is required' });
      return send(200, activationSessions.getSubscription(deviceId));
    }

    if (p === '/api/listing') return send(200, JSON.parse(await upstream('/list2.php')));
    if (p === '/api/archive') return send(200, JSON.parse(await upstream('/list2.php?archive=1')));

    if (p === '/api/day') {
      const date = Number(urlObj.searchParams.get('date'));
      if (!Number.isFinite(date) || date <= 0) return send(400, { error: 'bad date' });
      return send(200, JSON.parse(await upstream(`/list.php?date=${Math.floor(date)}`)));
    }

    if (p === '/api/week') {
      return send(200, parseScheduleWeek(await upstream('/schedule/')));
    }

    if (p === '/api/broadcast') {
      const bpath = urlObj.searchParams.get('path') || '';
      if (!/^\/[a-z0-9/_-]+\/$/.test(bpath)) return send(400, { error: 'bad path' });
      return send(200, parseBroadcast(await upstream(bpath)));
    }

    if (p === '/api/news') {
      const cat = urlObj.searchParams.get('cat') || '';
      const page = Math.max(1, Number(urlObj.searchParams.get('page')) || 1);
      if (cat && !NEWS_CATS.has(cat)) return send(400, { error: 'bad category' });
      const upath = `/news/${cat ? cat + '/' : ''}${page > 1 ? `?PAGEN_1=${page}` : ''}`;
      return send(200, parseNewsList(await upstream(upath)));
    }

    if (p === '/api/article') {
      const apath = urlObj.searchParams.get('path') || '';
      if (!/^\/news\/[a-z0-9/_-]+\/$/.test(apath)) return send(400, { error: 'bad path' });
      return send(200, parsePage(await upstream(apath)));
    }

    if (p === '/api/page') {
      const ppath = urlObj.searchParams.get('path') || '';
      if (!PAGE_WHITELIST.has(ppath)) return send(400, { error: 'bad path' });
      return send(200, parsePage(await upstream(ppath)));
    }

    send(404, { error: 'not found' });
  } catch (err) {
    send(502, { error: String(err.message || err) });
  }
}

function readJsonBody(req, maxBytes = 16 * 1024) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let size = 0;
    req.on('data', (chunk) => {
      size += chunk.length;
      if (size > maxBytes) {
        reject(new Error('request body too large'));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => {
      try {
        resolve(JSON.parse(Buffer.concat(chunks).toString('utf8') || '{}'));
      } catch {
        reject(new Error('invalid JSON'));
      }
    });
    req.on('error', reject);
  });
}

/* ---------- статика ---------- */
function serveStatic(req, res, urlObj) {
  const safe = path.normalize(decodeURIComponent(urlObj.pathname)).replace(/^(\.\.[/\\])+/, '');
  let file = path.join(ROOT, safe);
  if (safe === '/' || safe === '\\') file = path.join(ROOT, 'index.html');

  fs.readFile(file, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('404 Not Found');
      return;
    }
    const headers = {
      'Content-Type': MIME[path.extname(file)] || 'application/octet-stream',
      'X-Content-Type-Options': 'nosniff',
    };
    if (path.basename(file) === 'activate.html') headers['Cache-Control'] = 'no-store';
    res.writeHead(200, headers);
    res.end(data);
  });
}

function createAppServer() {
  return http.createServer((req, res) => {
    const urlObj = new URL(req.url, `http://${req.headers.host || `localhost:${PORT}`}`);
    if (urlObj.pathname.startsWith('/api/')) handleApi(req, res, urlObj);
    else serveStatic(req, res, urlObj);
  });
}

if (require.main === module) {
  createAppServer().listen(PORT, () => console.log(`SPORT TV website on http://localhost:${PORT}`));
}

module.exports = { createAppServer };
