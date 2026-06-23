-- =============================================================
--  Логическая схема базы данных  SvoySport TV
--  СУБД: PostgreSQL 15+
--  Создание: 2026-05-25
-- =============================================================

-- -------------------------------------------------------------
-- Справочник пользователей
-- -------------------------------------------------------------
CREATE TABLE users (
    id          SERIAL          PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    role        VARCHAR(50)     NOT NULL DEFAULT 'viewer'
                                CHECK (role IN ('viewer', 'admin')),
    language    VARCHAR(10)     NOT NULL DEFAULT 'ru'
                                CHECK (language IN ('ru', 'en', 'be')),
    sport_pref  VARCHAR(100)[],          -- массив предпочитаемых видов спорта
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  users             IS 'Зарегистрированные пользователи приложения';
COMMENT ON COLUMN users.role        IS 'Роль: viewer — обычный пользователь, admin — администратор';
COMMENT ON COLUMN users.sport_pref  IS 'Массив видов спорта (football, hockey, basketball …)';

-- -------------------------------------------------------------
-- Справочник спортивных событий
-- -------------------------------------------------------------
CREATE TABLE events (
    id          SERIAL          PRIMARY KEY,
    ext_id      VARCHAR(100)    NOT NULL UNIQUE,   -- ID на sport-tv.by
    title       VARCHAR(255)    NOT NULL,
    league      VARCHAR(150),
    start_time  TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_events_start_time ON events (start_time);
CREATE INDEX idx_events_league     ON events (league);

COMMENT ON TABLE  events         IS 'Спортивные события, агрегируемые с sport-tv.by';
COMMENT ON COLUMN events.ext_id  IS 'Внешний идентификатор события на источнике';

-- -------------------------------------------------------------
-- Трансляции (один event → несколько качеств)
-- -------------------------------------------------------------
CREATE TABLE streams (
    id          SERIAL          PRIMARY KEY,
    event_id    INTEGER         NOT NULL
                                REFERENCES events(id) ON DELETE CASCADE,
    url         TEXT            NOT NULL,           -- HLS / DASH ссылка
    quality     VARCHAR(20)     NOT NULL DEFAULT 'auto'
                                CHECK (quality IN ('auto', '360p', '480p', '720p', '1080p')),
    status      VARCHAR(20)     NOT NULL DEFAULT 'offline'
                                CHECK (status IN ('online', 'offline', 'scheduled'))
);

CREATE INDEX idx_streams_event_id ON streams (event_id);

COMMENT ON TABLE  streams          IS 'Видеопотоки (HLS/DASH) для каждого события';
COMMENT ON COLUMN streams.url      IS 'Прямая ссылка на m3u8 или mpd манифест';
COMMENT ON COLUMN streams.quality  IS 'Качество: auto выбирается плеером автоматически';

-- -------------------------------------------------------------
-- Сессии просмотра
-- -------------------------------------------------------------
CREATE TABLE sessions (
    id          SERIAL          PRIMARY KEY,
    user_id     INTEGER         NOT NULL
                                REFERENCES users(id)   ON DELETE CASCADE,
    stream_id   INTEGER         NOT NULL
                                REFERENCES streams(id) ON DELETE CASCADE,
    started_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    ended_at    TIMESTAMPTZ,                        -- NULL пока сессия активна
    CONSTRAINT chk_session_dates CHECK (ended_at IS NULL OR ended_at > started_at)
);

CREATE INDEX idx_sessions_user_id   ON sessions (user_id);
CREATE INDEX idx_sessions_stream_id ON sessions (stream_id);
CREATE INDEX idx_sessions_started   ON sessions (started_at DESC);

COMMENT ON TABLE  sessions           IS 'История просмотра трансляций пользователями';
COMMENT ON COLUMN sessions.ended_at  IS 'NULL означает активную (незавершённую) сессию';

-- -------------------------------------------------------------
-- Избранные события (M:N  users ↔ events)
-- -------------------------------------------------------------
CREATE TABLE favorites (
    user_id     INTEGER         NOT NULL
                                REFERENCES users(id)  ON DELETE CASCADE,
    event_id    INTEGER         NOT NULL
                                REFERENCES events(id) ON DELETE CASCADE,
    added_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, event_id)
);

CREATE INDEX idx_favorites_event_id ON favorites (event_id);

COMMENT ON TABLE favorites IS 'Избранные события: связь многие-ко-многим users ↔ events';

-- -------------------------------------------------------------
-- Конфигурация внешних API-эндпоинтов
-- -------------------------------------------------------------
CREATE TABLE endpoint_config (
    id          SERIAL          PRIMARY KEY,
    api_url     TEXT            NOT NULL,
    api_key     TEXT            NOT NULL,
    description VARCHAR(255),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE endpoint_config IS 'Настройки подключения к внешним источникам (sport-tv.by и др.)';

-- =============================================================
--  Начальные данные
-- =============================================================

-- Пример конфигурации источника данных
INSERT INTO endpoint_config (api_url, api_key, description)
VALUES
    ('https://sport-tv.by/list.php', 'demo_key_replace_me', 'Основной источник расписания');

-- Демо-пользователь (администратор)
INSERT INTO users (email, role, language, sport_pref)
VALUES ('admin@svoysport.tv', 'admin', 'ru', ARRAY['football', 'hockey']);
