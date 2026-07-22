/* ============ SPORT TV — данные (через /api-прокси serve.js) ============ */
(function () {
  'use strict';

  const DIRECT = 'https://sport-tv.by'; // list2.php отдаёт CORS — запасной путь без прокси
  const TZ = 'Europe/Minsk';

  const SPORTS = [
    { key: 'all',        label: 'Все виды спорта', news: '' },
    { key: 'football',   label: 'Футбол',       news: 'futbol' },
    { key: 'hockey',     label: 'Хоккей',       news: 'hokkey' },
    { key: 'handball',   label: 'Гандбол',      news: 'gandbol' },
    { key: 'basketball', label: 'Баскетбол',    news: 'basketbol' },
    { key: 'volleyball', label: 'Волейбол',     news: 'volleyball' },
    { key: 'other',      label: 'Другой спорт', news: 'drugoy-sport' },
  ];

  const SPORT_WORDS = {
    football: ['футбол'], hockey: ['хоккей'], handball: ['гандбол'],
    basketball: ['баскетбол'], volleyball: ['волейбол'],
  };

  function sportOf(item) {
    const t = (item.title || '').toLowerCase();
    if (t.includes('мини-футбол')) return 'other';
    for (const [key, words] of Object.entries(SPORT_WORDS)) {
      if (words.some((w) => t.includes(w))) return key;
    }
    return 'other';
  }

  function sportLabel(key) {
    const s = SPORTS.find((x) => x.key === key);
    return s ? s.label : 'Спорт';
  }

  /* Нормализованная модель трансляции */
  function normalize(raw) {
    const start = Number(raw.date) * 1000;
    const end = Number(raw.date_end) * 1000;
    return {
      id: raw.id || `${raw.date}-${raw.title}`,
      title: raw.title || '',
      annotation: raw.annotation || '',
      start,
      end,
      sport: sportOf(raw),
      thumbnail: raw.thumbnail || '',
      poster: typeof raw.poster === 'string' ? raw.poster : '',
      source: raw.source && raw.source !== 'no' ? raw.source : null,
      isPaid: Boolean(raw.isPaid),
      isArchive: Boolean(raw.is_archive),
      slider: Boolean(raw.slider),
      duration: raw.duration ? Number(raw.duration) : null,
    };
  }

  function isLive(item, now = Date.now()) {
    return !item.isArchive && item.start <= now && now < item.end;
  }

  async function getJson(url) {
    const res = await fetch(url, { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error(`HTTP ${res.status} для ${url}`);
    return res.json();
  }

  /** /api через serve.js; для лент list2 есть прямой CORS-фолбэк. */
  async function feed(apiUrl, directUrl) {
    try {
      const data = await getJson(apiUrl);
      return Array.isArray(data) ? data.map(normalize) : [];
    } catch (err) {
      if (!directUrl) throw err;
      const data = await getJson(directUrl);
      return Array.isArray(data) ? data.map(normalize) : [];
    }
  }

  /* Раздел оригинала по префиксу URL — точнее, чем эвристика по названию */
  const PATH_SPORT = {
    'futbol': 'football', 'hokkey': 'hockey', 'gandbol': 'handball',
    'basketbol': 'basketball', 'volleyball': 'volleyball', 'drugoy-sport': 'other',
  };

  const dayCache = new Map();
  const newsCache = new Map();
  let weekCache = null;

  const Api = {
    SPORTS,
    TZ,
    sportLabel,
    isLive,

    /** Витрина: live + ближайшие. */
    fetchListing() { return feed('/api/listing', DIRECT + '/list2.php'); },

    /** Архив записей с DVR-HLS источниками. */
    fetchArchive() { return feed('/api/archive', DIRECT + '/list2.php?archive=1'); },

    /** Расписание на конкретный день (list.php через прокси — без CORS напрямую). */
    async fetchDay(dateSec) {
      if (dayCache.has(dateSec)) return dayCache.get(dateSec);
      const items = await feed(`/api/day?date=${dateSec}`);
      dayCache.set(dateSec, items);
      return items;
    },

    /**
     * Полное расписание с /schedule/ оригинала:
     * { today: [...незавершённые сегодня], todayDate, week: [{date, items}] }.
     * Каждый item: { day, time, title, path, image, startMs }.
     */
    async fetchWeek() {
      if (weekCache) return weekCache;
      weekCache = await getJson('/api/week');
      weekCache.today.forEach((i) => { i.sport = Api.sportFromPath(i.path); });
      weekCache.week.forEach((d) => d.items.forEach((i) => { i.sport = Api.sportFromPath(i.path); }));
      return weekCache;
    },

    /** Данные страницы трансляции оригинала: { title, file, poster, date }. */
    fetchBroadcast(path) {
      return getJson(`/api/broadcast?path=${encodeURIComponent(path)}`);
    },

    sportFromPath(path) {
      return PATH_SPORT[(path || '').split('/')[1]] || 'other';
    },

    /** Новости: cat — slug оригинала ('' = все), page с 1. */
    async fetchNews(cat, page) {
      const key = `${cat}|${page}`;
      if (newsCache.has(key)) return newsCache.get(key);
      const data = await getJson(`/api/news?cat=${encodeURIComponent(cat)}&page=${page}`);
      newsCache.set(key, data);
      return data;
    },

    /** Статья новости: { title, html } (очищено на сервере). */
    fetchArticle(path) {
      return getJson(`/api/article?path=${encodeURIComponent(path)}`);
    },

    /** Инфо-страница оригинала (оферта, оплата, правообладателям). */
    fetchPage(path) {
      return getJson(`/api/page?path=${encodeURIComponent(path)}`);
    },

    fmtTime(ms) {
      return new Intl.DateTimeFormat('ru-RU', {
        timeZone: TZ, hour: '2-digit', minute: '2-digit',
      }).format(new Date(ms));
    },

    fmtDate(ms) {
      return new Intl.DateTimeFormat('ru-RU', {
        timeZone: TZ, day: 'numeric', month: 'long',
      }).format(new Date(ms));
    },

    fmtWeekday(ms) {
      return new Intl.DateTimeFormat('ru-RU', { timeZone: TZ, weekday: 'short' })
        .format(new Date(ms)).replace('.', '');
    },

    fmtDuration(sec) {
      if (!sec) return '';
      const h = Math.floor(sec / 3600);
      const m = Math.round((sec % 3600) / 60);
      return h > 0 ? `${h} ч ${m} мин` : `${m} мин`;
    },
  };

  window.SportTvApi = Api;
})();
