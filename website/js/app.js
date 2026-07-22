/* ============ СВОЙ СПОРТ — приложение (роутинг, рендер, плеер) ============ */
(function () {
  'use strict';

  const Api = window.SportTvApi;
  const $ = (sel) => document.querySelector(sel);

  const state = {
    route: 'home',      // home | schedule | news | article | doc | archive | search | contacts
    sport: 'all',
    query: '',
    day: null,          // ключ выбранного дня 'YYYY-MM-DD'
    week: null,         // данные /api/week (полное расписание оригинала)
    watchId: null,      // id нашей трансляции для #/watch
    watchPath: '',      // путь трансляции оригинала для #/watch
    newsPage: 1,
    articlePath: '',    // /news/... для статьи
    docPath: '',        // /oferta/ | /payment/ | /informatsiya-dlya-pravoobladateley/
    listing: [],
    archive: [],
    heroItems: [],
    heroIndex: 0,
  };

  const DOC_ROUTES = {
    oferta: '/oferta/',
    payment: '/payment/',
    rights: '/informatsiya-dlya-pravoobladateley/',
  };

  let heroTimer = null;
  let hls = null;

  /* ---------- Роутинг: состояние в URL (#/schedule, #/sport/football, #/search?q=) ---------- */
  function parseHash() {
    const hash = decodeURIComponent(location.hash.replace(/^#\/?/, ''));
    const [path, qs] = hash.split('?');
    const params = new URLSearchParams(qs || '');
    const seg = path.split('/').filter(Boolean);

    state.query = params.get('q') || '';
    if (seg[0] === 'sport' && seg[1]) {
      state.route = 'home';
      state.sport = Api.SPORTS.some((s) => s.key === seg[1]) ? seg[1] : 'all';
    } else if (seg[0] === 'article') {
      state.route = 'article';
      state.articlePath = params.get('path') || '';
    } else if (seg[0] === 'watch') {
      state.route = 'watch';
      state.watchId = params.get('id') || null;
      state.watchPath = params.get('p') || '';
    } else if (DOC_ROUTES[seg[0]]) {
      state.route = 'doc';
      state.docPath = DOC_ROUTES[seg[0]];
    } else if (['schedule', 'news', 'archive', 'contacts', 'search'].includes(seg[0])) {
      state.route = seg[0];
      state.sport = params.get('sport') || state.sport;
      state.newsPage = Math.max(1, Number(params.get('page')) || 1);
    } else {
      state.route = 'home';
      state.sport = params.get('sport') || 'all';
    }
  }

  function buildHash() {
    const params = new URLSearchParams();
    if (state.route === 'search' && state.query) params.set('q', state.query);
    if (state.route === 'article' && state.articlePath) params.set('path', state.articlePath);
    if (state.route === 'news' && state.newsPage > 1) params.set('page', String(state.newsPage));
    if (state.route === 'watch') {
      if (state.watchId) params.set('id', state.watchId);
      else if (state.watchPath) params.set('p', state.watchPath);
    }
    if (state.sport !== 'all' && !['article', 'doc', 'watch'].includes(state.route)) params.set('sport', state.sport);
    const qs = params.toString();
    let path = state.route === 'home' ? '/' : `/${state.route}`;
    if (state.route === 'doc') {
      const slug = Object.keys(DOC_ROUTES).find((k) => DOC_ROUTES[k] === state.docPath) || 'oferta';
      path = `/${slug}`;
    }
    return `#${path}${qs ? '?' + qs : ''}`;
  }

  function navigate(mutate) {
    mutate();
    const next = buildHash();
    if (location.hash !== next) {
      location.hash = next; // hashchange вызовет render
    } else {
      render();
    }
  }

  /* ---------- Хелперы рендера ---------- */
  function el(html) {
    const t = document.createElement('template');
    t.innerHTML = html.trim();
    return t.content.firstElementChild;
  }

  const esc = (s) => String(s).replace(/[&<>"]/g, (c) =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

  function bySport(items) {
    const def = Api.SPORTS.find((s) => s.key === state.sport);
    if (!def || def.key === 'all') return items;
    return items.filter((i) => i.sport === state.sport);
  }

  const PLAY_SVG = '<svg viewBox="0 0 52 52"><circle cx="26" cy="26" r="25" fill="rgba(69,86,235,0.92)"/><path d="M21 17v18l14-9z" fill="#fff"/></svg>';

  function matchCard(item) {
    const live = Api.isLive(item);
    const badge = live
      ? '<span class="badge-live">LIVE</span>'
      : item.isArchive
        ? ''
        : `<span class="badge-soon">${Api.fmtDate(item.start)}, ${Api.fmtTime(item.start)}</span>`;
    const dur = item.duration ? `<span class="badge-duration">${Api.fmtDuration(item.duration)}</span>` : '';
    const card = el(`
      <button class="match-card" type="button">
        <div class="thumb">
          <img src="${esc(item.thumbnail)}" alt="" loading="lazy" width="320" height="180">
          <div class="badges">${badge}</div>
          ${dur}
          <div class="play-glyph">${PLAY_SVG}</div>
        </div>
        <div class="card-body">
          <span class="card-title">${esc(item.title)}</span>
          <span class="card-sub">${esc(item.annotation)}</span>
        </div>
      </button>`);
    card.addEventListener('click', () => openPlayer(item));
    return card;
  }

  /** Пункт полного расписания (/schedule/ оригинала): без точного времени конца. */
  function weekRow(item) {
    // «в эфире» = уже началась и оригинал ещё держит её в ленте незавершённых
    const live = item.startMs <= Date.now() &&
      Boolean(state.week && state.week.today.some((t) => t.path === item.path && t.startMs === item.startMs));
    const row = el(`
      <button class="schedule-item ${live ? 'is-live' : ''}" type="button">
        <div class="schedule-time">${esc(item.time)}<small>${Api.fmtDate(item.startMs)}</small></div>
        <div class="schedule-info">
          <div class="title">${esc(item.title)}</div>
        </div>
        <div class="schedule-side">
          ${live ? '<span class="badge-live">В ЭФИРЕ</span>' : ''}
          <span class="sport-tag">${Api.sportLabel(item.sport)}</span>
        </div>
      </button>`);
    row.addEventListener('click', () => {
      const own = findOwnBroadcast(item);
      if (own) {
        openPlayer(own);
      } else {
        navigate(() => {
          state.route = 'watch';
          state.watchId = null;
          state.watchPath = item.path;
        });
      }
    });
    return row;
  }

  /** Ищем трансляцию из нашей витрины (list2) по времени и названию — её можно открыть в своём плеере. */
  function findOwnBroadcast(weekItem) {
    const t = weekItem.title.toLowerCase();
    return [...state.listing, ...state.archive].find((b) =>
      Math.abs(b.start - weekItem.startMs) <= 20 * 60 * 1000 &&
      b.title.toLowerCase().includes(t));
  }

  function scheduleRow(item) {
    const live = Api.isLive(item);
    const row = el(`
      <button class="schedule-item ${live ? 'is-live' : ''}" type="button">
        <div class="schedule-time">${Api.fmtTime(item.start)}<small>${Api.fmtDate(item.start)}</small></div>
        <div class="schedule-info">
          <div class="title">${esc(item.title)}</div>
          <div class="sub">${esc(item.annotation)}</div>
        </div>
        <div class="schedule-side">
          ${live ? '<span class="badge-live">LIVE</span>' : ''}
          <span class="sport-tag">${Api.sportLabel(item.sport)}</span>
        </div>
      </button>`);
    row.addEventListener('click', () => openPlayer(item));
    return row;
  }

  function emptyState(text) {
    return el(`<div class="empty-state">${text}</div>`);
  }

  function skeletons(n) {
    const wrap = el('<div class="skeleton-row"></div>');
    for (let i = 0; i < n; i++) wrap.append(el('<div class="skeleton"></div>'));
    return wrap;
  }

  /* ---------- Панель видов спорта ---------- */
  function renderSportBar() {
    const bar = $('#sportBar');
    bar.innerHTML = '';
    for (const s of Api.SPORTS) {
      const chip = el(`<button class="sport-chip ${s.key === state.sport ? 'active' : ''}" role="tab"
        aria-selected="${s.key === state.sport}" type="button">${s.label}</button>`);
      chip.addEventListener('click', () => navigate(() => {
        state.sport = s.key;
        state.newsPage = 1;
      }));
      bar.append(chip);
    }
  }

  /* ---------- Hero ---------- */
  function renderHero() {
    const hero = $('#hero');
    const items = bySport(state.listing);
    const now = Date.now();
    const live = items.filter((i) => Api.isLive(i, now));
    const featured = live.length
      ? live
      : items.filter((i) => i.slider && i.start > now).slice(0, 5);
    state.heroItems = (featured.length ? featured : items.slice(0, 5)).slice(0, 5);

    clearInterval(heroTimer);
    if (!state.heroItems.length) { hero.hidden = true; return; }
    hero.hidden = false;
    state.heroIndex = Math.min(state.heroIndex, state.heroItems.length - 1);
    drawHeroSlide();
    if (state.heroItems.length > 1) {
      heroTimer = setInterval(() => {
        state.heroIndex = (state.heroIndex + 1) % state.heroItems.length;
        drawHeroSlide();
      }, 8000);
    }
  }

  function drawHeroSlide() {
    const item = state.heroItems[state.heroIndex];
    if (!item) return;
    const live = Api.isLive(item);
    $('#heroBg').style.backgroundImage = `url("${item.thumbnail}")`;

    const info = $('#heroInfo');
    info.innerHTML = '';
    info.append(el(`
      <div>
        <div class="hero-kicker">
          ${live ? '<span class="badge-live">LIVE</span> Прямой эфир' : 'Скоро в эфире'}
        </div>
        <h1 class="hero-title">${esc(item.title)}</h1>
        <div class="hero-meta">
          <span class="meta-chip">${Api.sportLabel(item.sport)}</span>
          <span class="meta-chip">${Api.fmtDate(item.start)} · ${Api.fmtTime(item.start)}</span>
          ${item.isPaid ? '<span class="meta-chip">🔒 По подписке</span>' : '<span class="meta-chip">Бесплатно</span>'}
        </div>
        <div class="hero-actions">
          <button class="btn-play" type="button">
            <svg viewBox="0 0 24 24"><path d="M8 5v14l11-7z" fill="currentColor"/></svg>
            ${live ? 'Смотреть эфир' : 'Подробнее'}
          </button>
        </div>
      </div>`));
    info.querySelector('.btn-play').addEventListener('click', () => openPlayer(item));

    $('#heroPoster').innerHTML =
      `<img src="${esc(item.thumbnail)}" alt="${esc(item.title)}" width="640" height="360" fetchpriority="high">`;

    const dots = $('#heroDots');
    dots.innerHTML = '';
    state.heroItems.forEach((_, i) => {
      const d = el(`<button class="${i === state.heroIndex ? 'active' : ''}" aria-label="Слайд ${i + 1}" type="button"></button>`);
      d.addEventListener('click', () => { state.heroIndex = i; drawHeroSlide(); });
      dots.append(d);
    });
  }

  /* ---------- Главная ---------- */
  function renderHome() {
    renderHero();
    const now = Date.now();
    const items = bySport(state.listing);

    const live = items.filter((i) => Api.isLive(i, now));
    $('#liveSection').hidden = live.length === 0;
    const liveRow = $('#liveRow');
    liveRow.innerHTML = '';
    live.forEach((i) => liveRow.append(matchCard(i)));

    // Сегодняшняя лента как у оригинала: все трансляции, которые ещё не закончились
    const list = $('#upcomingList');
    list.innerHTML = '';
    if (state.week) {
      const today = state.week.today.filter(
        (i) => state.sport === 'all' || i.sport === state.sport);
      if (today.length) today.forEach((i) => list.append(weekRow(i)));
      else list.append(emptyState('Сегодня в этом разделе трансляций больше нет'));
    } else {
      list.append(skeletons(6));
    }

    const arch = bySport(state.archive).slice(0, 10);
    $('#homeArchiveSection').hidden = arch.length === 0;
    const archRow = $('#homeArchiveRow');
    archRow.innerHTML = '';
    arch.forEach((i) => archRow.append(matchCard(i)));
  }

  /* ---------- Расписание на неделю (данные /schedule/ оригинала) ---------- */
  function renderScheduleView() {
    const chips = $('#dayChips');
    const list = $('#scheduleList');
    chips.innerHTML = '';
    list.innerHTML = '';

    if (!state.week) { list.append(skeletons(8)); return; } // дорисуется после boot()

    const days = state.week.week;
    if (!days.length) { list.append(emptyState('Расписание временно недоступно')); return; }

    const todayKey = state.week.todayDate;
    if (!state.day || !days.some((d) => d.date === state.day)) {
      state.day = days.some((d) => d.date === todayKey) ? todayKey : days[days.length - 1].date;
    }

    for (const d of days) {
      const ms = Date.parse(`${d.date}T12:00:00+03:00`);
      const label = d.date === todayKey ? 'Сегодня' : Api.fmtWeekday(ms);
      const chip = el(`<button class="day-chip ${d.date === state.day ? 'active' : ''}" role="tab"
        aria-selected="${d.date === state.day}" type="button">${label}<b>${Api.fmtDate(ms)}</b></button>`);
      chip.addEventListener('click', () => { state.day = d.date; renderScheduleView(); });
      chips.append(chip);
    }

    const day = days.find((d) => d.date === state.day);
    const now = Date.now();
    const items = day.items.filter((i) => {
      if (state.sport !== 'all' && i.sport !== state.sport) return false;
      // как у оригинала в ленте дня: для сегодняшнего дня скрываем завершённые
      if (day.date === todayKey) {
        return state.week.today.some((t) => t.path === i.path && t.startMs === i.startMs) || i.startMs > now;
      }
      return true;
    });

    if (!items.length) {
      list.append(emptyState('На этот день трансляций не запланировано'));
      return;
    }
    items.forEach((i) => list.append(weekRow(i)));
  }

  /* ---------- Архив ---------- */
  function renderArchiveView() {
    const grid = $('#archiveGrid');
    grid.innerHTML = '';
    const items = bySport(state.archive);
    if (!items.length) {
      grid.append(emptyState('В архиве этого раздела пока нет записей'));
      return;
    }
    items.forEach((i) => grid.append(matchCard(i)));
  }

  /* ---------- Поиск ---------- */
  function renderSearchView() {
    $('#searchTitle').textContent = state.query
      ? `Поиск: «${state.query}»` : 'Поиск';
    const box = $('#searchResults');
    box.innerHTML = '';
    const q = state.query.trim().toLowerCase();
    if (!q) { box.append(emptyState('Введите запрос — найдём трансляции и записи')); return; }

    const seen = new Set();
    const own = [...state.listing, ...state.archive].filter((i) => {
      const key = i.id + i.title;
      if (seen.has(key)) return false;
      seen.add(key);
      return (i.title + ' ' + i.annotation).toLowerCase().includes(q);
    }).sort((a, b) => b.start - a.start);

    // + вся неделя ретрансляций, кроме дублей наших эфиров
    const weekItems = state.week
      ? state.week.week.flatMap((d) => d.items)
          .filter((i) => i.title.toLowerCase().includes(q) && !findOwnBroadcast(i))
          .sort((a, b) => a.startMs - b.startMs)
      : [];

    if (!own.length && !weekItems.length) {
      box.append(emptyState(`По запросу «${esc(state.query)}» ничего не найдено`));
      return;
    }
    own.forEach((i) => box.append(scheduleRow(i)));
    weekItems.forEach((i) => box.append(weekRow(i)));
  }

  /* ---------- Новости ---------- */
  function newsCard(item) {
    const card = el(`
      <a class="news-card" href="#/article?path=${encodeURIComponent(item.path)}">
        <div class="thumb">
          <img src="${esc(item.image)}" alt="" loading="lazy" width="320" height="180">
        </div>
        <div class="card-body">
          <div class="news-meta">
            <span class="sport-tag">${esc(item.category)}</span>
            <span class="muted">${esc(item.date)}</span>
          </div>
          <span class="card-title">${esc(item.title)}</span>
        </div>
      </a>`);
    return card;
  }

  function renderNewsView() {
    const grid = $('#newsGrid');
    const pag = $('#newsPagination');
    grid.innerHTML = '';
    pag.innerHTML = '';
    grid.append(skeletons(4));

    const sportDef = Api.SPORTS.find((s) => s.key === state.sport);
    const cat = sportDef ? sportDef.news : '';
    const requested = `${cat}|${state.newsPage}`;

    Api.fetchNews(cat, state.newsPage)
      .then((data) => {
        const nowDef = Api.SPORTS.find((s) => s.key === state.sport);
        if (state.route !== 'news' || `${nowDef ? nowDef.news : ''}|${state.newsPage}` !== requested) return;
        grid.innerHTML = '';
        if (!data.items.length) {
          grid.append(emptyState('Новостей в этом разделе пока нет'));
          return;
        }
        data.items.forEach((i) => grid.append(newsCard(i)));

        if (data.pages > 1) {
          for (let p = 1; p <= data.pages; p++) {
            const b = el(`<button class="page-btn ${p === state.newsPage ? 'active' : ''}" type="button">${p}</button>`);
            b.addEventListener('click', () => navigate(() => { state.newsPage = p; }));
            pag.append(b);
          }
        }
      })
      .catch(() => {
        grid.innerHTML = '';
        grid.append(emptyState('Не удалось загрузить новости. Попробуйте обновить страницу.'));
      });
  }

  function renderArticleView() {
    const box = $('#articleBody');
    box.innerHTML = '';
    box.append(skeletons(3));
    const requested = state.articlePath;
    if (!requested) { box.innerHTML = ''; box.append(emptyState('Статья не найдена')); return; }

    Api.fetchArticle(requested)
      .then((data) => {
        if (state.articlePath !== requested || state.route !== 'article') return;
        box.innerHTML = `<h1 class="page-title">${esc(data.title)}</h1><div class="doc-content">${data.html}</div>`;
        window.scrollTo({ top: 0 });
      })
      .catch(() => {
        box.innerHTML = '';
        box.append(emptyState('Не удалось загрузить статью'));
      });
  }

  function renderDocView() {
    const box = $('#docBody');
    box.innerHTML = '';
    box.append(skeletons(3));
    const requested = state.docPath;

    Api.fetchPage(requested)
      .then((data) => {
        if (state.docPath !== requested || state.route !== 'doc') return;
        box.innerHTML = `<h1 class="page-title">${esc(data.title)}</h1><div class="doc-content">${data.html}</div>`;
        window.scrollTo({ top: 0 });
      })
      .catch(() => {
        box.innerHTML = '';
        box.append(emptyState('Не удалось загрузить страницу'));
      });
  }

  /* ---------- Страница трансляции (#/watch) ---------- */
  /** Все клики по трансляциям ведут на отдельную страницу с плеером, как у оригинала. */
  function openPlayer(item) {
    navigate(() => {
      state.route = 'watch';
      state.watchId = String(item.id);
      state.watchPath = '';
    });
  }

  function renderWatchView() {
    stopVideo();
    const video = $('#watchVideo');
    const frame = $('#watchFrame');
    const notice = $('#watchNotice');
    const meta = $('#watchMeta');
    video.hidden = true;
    notice.hidden = true;
    meta.innerHTML = '';

    if (state.watchId) {
      const item = [...state.listing, ...state.archive]
        .find((b) => String(b.id) === String(state.watchId));
      if (!item) {
        // данные ещё грузятся — render() повторится после boot()
        if (!state.listing.length && !state.archive.length) {
          showNotice(notice, video, 'Загружаем трансляцию…');
        } else {
          showNotice(notice, video, '<span class="big">Трансляция не найдена</span>Возможно, эфир уже убран из витрины.');
        }
        return;
      }
      meta.innerHTML = `
        <div class="title">${esc(item.title)}</div>
        <div class="sub">${esc(item.annotation)} · ${Api.fmtDate(item.start)}, ${Api.fmtTime(item.start)}
        ${item.duration ? ' · ' + Api.fmtDuration(item.duration) : ''}</div>`;
      if (item.thumbnail) video.poster = item.thumbnail;

      if (item.isPaid) {
        showNotice(notice, video, '<span class="big">🔒 Трансляция по подписке</span>Оформите подписку в приложении «Свой Спорт», чтобы смотреть платные трансляции.');
      } else if (!item.source) {
        showNotice(notice, video, Api.isLive(item)
          ? '<span class="big">Эфир скоро появится</span>Источник трансляции ещё не опубликован — обновите страницу через пару минут.'
          : `<span class="big">Трансляция ещё не началась</span>Начало — ${Api.fmtDate(item.start)} в ${Api.fmtTime(item.start)} (по минскому времени).`);
      } else {
        playHls(item.source, video, notice);
      }
      return;
    }

    if (!state.watchPath) {
      showNotice(notice, video, '<span class="big">Трансляция не найдена</span>');
      return;
    }

    showNotice(notice, video, 'Загружаем трансляцию…');
    const requested = state.watchPath;
    Api.fetchBroadcast(requested)
      .then((b) => {
        if (state.route !== 'watch' || state.watchPath !== requested) return;
        meta.innerHTML = `
          <div class="title">${esc(b.title || 'Трансляция')}</div>
          <div class="sub">${esc(b.date || '')}</div>`;
        if (b.poster) video.poster = b.poster;

        if (!b.file) {
          showNotice(notice, video, '<span class="big">Трансляция ещё не началась</span>Плеер появится ближе к началу эфира — загляните позже.');
          return;
        }
        const yt = /(?:youtu\.be\/|watch\?v=|youtube\.com\/(?:embed|live|shorts)\/)([\w-]{6,})/.exec(b.file);
        if (yt) {
          notice.hidden = true;
          frame.hidden = false;
          frame.src = `https://www.youtube.com/embed/${yt[1]}?autoplay=1`;
        } else if (/\.m3u8/.test(b.file)) {
          playHls(b.file, video, notice);
        } else {
          showNotice(notice, video, `<span class="big">Плеер этого источника недоступен</span>
            <a class="btn btn-filled" target="_blank" rel="noopener"
               href="https://sport-tv.by${esc(requested)}">Смотреть на sport-tv.by</a>`);
        }
      })
      .catch(() => {
        if (state.route !== 'watch' || state.watchPath !== requested) return;
        showNotice(notice, video, '<span class="big">Не удалось загрузить трансляцию</span>Попробуйте обновить страницу.');
      });
  }

  function playHls(src, video, notice) {
    notice.hidden = true;
    video.hidden = false;
    if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = src; // Safari — нативный HLS
      video.play().catch(() => {});
    } else if (window.Hls && Hls.isSupported()) {
      hls = new Hls();
      hls.loadSource(src);
      hls.attachMedia(video);
      hls.on(Hls.Events.MANIFEST_PARSED, () => video.play().catch(() => {}));
      hls.on(Hls.Events.ERROR, (_e, data) => {
        if (data.fatal) {
          showNotice(notice, video, '<span class="big">Не удалось запустить видео</span>Попробуйте ещё раз чуть позже.');
        }
      });
    } else {
      showNotice(notice, video, '<span class="big">Браузер не поддерживает HLS</span>Откройте трансляцию в Safari или обновите браузер.');
    }
  }

  function showNotice(notice, video, html) {
    video.hidden = true;
    notice.hidden = false;
    notice.innerHTML = `<div>${html}</div>`;
  }

  function stopVideo() {
    if (hls) { hls.destroy(); hls = null; }
    for (const id of ['playerVideo', 'watchVideo']) {
      const v = document.getElementById(id);
      if (!v) continue;
      v.pause();
      v.removeAttribute('src');
      v.load();
    }
    const frame = document.getElementById('watchFrame');
    if (frame) { frame.removeAttribute('src'); frame.hidden = true; }
  }

  function closeModals() {
    stopVideo();
    document.querySelectorAll('.modal').forEach((m) => { m.hidden = true; });
    document.body.style.overflow = '';
  }

  /* ---------- Общий рендер ---------- */
  function render() {
    parseHash();
    renderSportBar();

    document.querySelectorAll('.main-nav a').forEach((a) => {
      a.classList.toggle('active', a.dataset.nav === state.route);
    });
    document.querySelectorAll('.view').forEach((v) => { v.hidden = true; });
    $(`#view-${state.route}`).hidden = false;

    if (state.route === 'home') renderHome();
    if (state.route === 'schedule') renderScheduleView();
    if (state.route === 'news') renderNewsView();
    if (state.route === 'article') renderArticleView();
    if (state.route === 'doc') renderDocView();
    if (state.route === 'archive') renderArchiveView();
    if (state.route === 'watch') renderWatchView();
    if (state.route === 'search') renderSearchView();
    if (state.route !== 'home') clearInterval(heroTimer);
    if (state.route !== 'watch') stopVideo(); // уход со страницы плеера останавливает эфир
  }

  /* ---------- Инициализация ---------- */
  async function boot() {
    render(); // мгновенный каркас, данные подтянутся

    try {
      const [listing, archive, week] = await Promise.all([
        Api.fetchListing(),
        Api.fetchArchive(),
        Api.fetchWeek().catch(() => null), // без week главная и поиск всё равно живут
      ]);
      state.listing = listing;
      state.archive = archive;
      state.week = week;
    } catch (err) {
      console.error('Не удалось загрузить данные sport-tv.by:', err);
      $('#upcomingList').innerHTML = '';
      $('#upcomingList').append(
        emptyState('Не удалось загрузить трансляции. Проверьте соединение и обновите страницу.'));
      return;
    }
    render();
    // раз в минуту обновляем статусы LIVE (только на главной, чтобы не сбивать чтение)
    setInterval(() => { if (state.route === 'home') render(); }, 60_000);
  }

  window.addEventListener('hashchange', render);

  /* Поиск из шапки */
  let searchDebounce = null;
  $('#searchInput').addEventListener('input', (e) => {
    clearTimeout(searchDebounce);
    const value = e.target.value;
    searchDebounce = setTimeout(() => {
      navigate(() => {
        state.query = value;
        state.route = value.trim() ? 'search' : 'home';
      });
    }, 250);
  });

  /* Модалки */
  document.querySelectorAll('[data-close]').forEach((b) =>
    b.addEventListener('click', closeModals));
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModals();
  });

  /* Вход */
  $('#loginBtn').addEventListener('click', () => {
    $('#loginModal').hidden = false;
    document.body.style.overflow = 'hidden';
    $('#loginPhone').focus();
  });
  $('#loginForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const note = $('#loginNote');
    note.hidden = false;
    note.textContent = 'Код отправлен! Завершите вход в приложении «Свой Спорт» — авторизация на сайте появится в ближайшем обновлении.';
  });

  /* Подписка на рассылку */
  $('#subscribeBtn').addEventListener('click', () => {
    $('#subscribeModal').hidden = false;
    document.body.style.overflow = 'hidden';
    $('#subscribeEmail').focus();
  });
  $('#subscribeForm').addEventListener('submit', (e) => {
    e.preventDefault();
    const note = $('#subscribeNote');
    note.hidden = false;
    note.textContent = `Готово! ${$('#subscribeEmail').value} добавлен в рассылку анонсов «Свой Спорт».`;
  });

  /* Мобильное меню */
  $('#burgerBtn').addEventListener('click', () => {
    const nav = $('.main-nav');
    const open = nav.classList.toggle('open');
    $('#burgerBtn').setAttribute('aria-expanded', String(open));
  });
  document.querySelectorAll('.main-nav a').forEach((a) =>
    a.addEventListener('click', () => $('.main-nav').classList.remove('open')));

  boot();
})();
