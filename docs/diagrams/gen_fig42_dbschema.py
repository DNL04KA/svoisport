"""Рисунок 4.2 — Логическая схема базы данных SvoySport TV"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(18, 12))
ax.set_xlim(0, 18)
ax.set_ylim(0, 12)
ax.axis('off')
fig.patch.set_facecolor('white')

TW    = 3.2    # ширина таблицы
RH    = 0.37   # высота строки
HH    = 0.48   # высота заголовка
CW    = 0.054  # ширина символа (ед.) при fontsize 7.5 italic


# ── Таблица ───────────────────────────────────────────────────────────────

def draw_table(cx, cy, name, fields):
    """fields = [(field_name, key)]  key ∈ {'PK','FK',''}"""
    n   = len(fields)
    h   = HH + n * RH
    x0  = cx - TW / 2
    top = cy + h / 2

    ax.add_patch(mpatches.Rectangle(
        (x0, top - h), TW, h,
        fill=False, edgecolor='black', linewidth=1.1, zorder=3))

    # заголовок курсивом
    ax.text(cx, top - HH / 2, name,
            ha='center', va='center',
            fontsize=9, style='italic', color='black', zorder=4)
    ax.plot([x0, x0 + TW], [top - HH, top - HH], 'k-', lw=0.9)

    # поля
    for i, (fname, key) in enumerate(fields):
        ry      = top - HH - (i + 0.5) * RH
        row_bot = top - HH - (i + 1) * RH

        if i < n - 1:
            ax.plot([x0, x0 + TW], [row_bot, row_bot],
                    color='#C0C0C0', lw=0.5, zorder=3)

        text_x = x0 + 0.54

        if key in ('PK', 'FK'):
            ax.text(x0 + 0.14, ry, key,
                    ha='left', va='center',
                    fontsize=7, fontweight='bold', color='black', zorder=4)

        ax.text(text_x, ry, fname,
                ha='left', va='center',
                fontsize=7.5, style='italic', color='black', zorder=4)

        # Подчёркивание PK-поля (рисуем вручную)
        if key == 'PK':
            line_len = len(fname) * CW
            ul_y     = ry - RH * 0.30
            ax.plot([text_x, text_x + line_len], [ul_y, ul_y],
                    'k-', lw=0.7, zorder=5)

    return dict(cx=cx, cy=cy,
                x0=x0, x1=x0 + TW,
                top=top, bot=top - h)


# ── Метки кардинальности ─────────────────────────────────────────────────

def one_mark(x, y, horiz=True):
    d = 0.09
    offsets = (0.0, -0.13) if horiz else (0.0, 0.13)
    for o in offsets:
        if horiz:
            ax.plot([x + o, x + o], [y - d, y + d], 'k-', lw=0.9)
        else:
            ax.plot([x - d, x + d], [y + o, y + o], 'k-', lw=0.9)


def crow(x, y, direction):
    d = 0.12
    if direction == 'right':
        for dy in (d, 0, -d):
            ax.plot([x, x + d * 1.9], [y, y + dy], 'k-', lw=0.9)
    elif direction == 'left':
        for dy in (d, 0, -d):
            ax.plot([x, x - d * 1.9], [y, y + dy], 'k-', lw=0.9)
    elif direction == 'up':
        for dx in (d, 0, -d):
            ax.plot([x, x + dx], [y, y + d * 1.9], 'k-', lw=0.9)
    elif direction == 'down':
        for dx in (d, 0, -d):
            ax.plot([x, x + dx], [y, y - d * 1.9], 'k-', lw=0.9)


def polyline(pts):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    ax.plot(xs, ys, 'k-', lw=0.9, zorder=2)


# ── Расположение таблиц ───────────────────────────────────────────────────

g_users = draw_table(2.5, 8.0, 'users', [
    ('id',         'PK'),
    ('email',      ''),
    ('role',       ''),
    ('language',   ''),
    ('sport_pref', ''),
    ('created_at', ''),
])

g_events = draw_table(9.0, 9.2, 'events', [
    ('id',         'PK'),
    ('ext_id',     ''),
    ('title',      ''),
    ('league',     ''),
    ('start_time', ''),
])

g_streams = draw_table(15.0, 8.5, 'streams', [
    ('id',       'PK'),
    ('event_id', 'FK'),
    ('url',      ''),
    ('quality',  ''),
    ('status',   ''),
])

g_sessions = draw_table(9.0, 5.2, 'sessions', [
    ('id',         'PK'),
    ('user_id',    'FK'),
    ('stream_id',  'FK'),
    ('started_at', ''),
    ('ended_at',   ''),
])

g_favs = draw_table(2.5, 3.0, 'favorites', [
    ('user_id',  'PK'),
    ('event_id', 'PK'),
    ('added_at', ''),
])

draw_table(15.0, 3.4, 'endpoint_config', [
    ('id',          'PK'),
    ('api_url',     ''),
    ('api_key',     ''),
    ('description', ''),
    ('updated_at',  ''),
])

# ── Связи 1 → N ───────────────────────────────────────────────────────────
# Crow's foot рисуется на стороне «многие»,
# линии расходятся НАРУЖУ от таблицы (к соединительной линии).

# 1. events(1) ──── streams(N)  streams.event_id → events.id
mid = 12.0
polyline([(g_events['x1'], 9.2), (mid, 9.2), (mid, 8.5), (g_streams['x0'], 8.5)])
one_mark(g_events['x1'], 9.2, horiz=True)    # || у правого края events
crow(g_streams['x0'], 8.5, 'left')            # foot наружу влево от streams

# 2. users(1) ──── sessions(N)  sessions.user_id → users.id
vx = 5.7
polyline([(g_users['x1'], 8.0), (vx, 8.0), (vx, 5.2), (g_sessions['x0'], 5.2)])
one_mark(g_users['x1'], 8.0, horiz=True)     # || у правого края users
crow(g_sessions['x0'], 5.2, 'left')           # foot наружу влево от sessions

# 3. streams(1) ──── sessions(N)  sessions.stream_id → streams.id
vy = 4.75
polyline([(g_streams['cx'], g_streams['bot']),
          (g_streams['cx'], vy),
          (g_sessions['x1'], vy)])
one_mark(g_streams['cx'], g_streams['bot'], horiz=False)  # || под streams
crow(g_sessions['x1'], vy, 'right')            # foot наружу вправо от sessions

# 4. users(1) ──── favorites(N)  favorites.user_id → users.id
polyline([(g_users['cx'], g_users['bot']), (g_users['cx'], g_favs['top'])])
one_mark(g_users['cx'], g_users['bot'], horiz=False)  # || под users
crow(g_users['cx'], g_favs['top'], 'up')       # foot наружу вверх от favorites

# 5. events(1) ──── favorites(N)  favorites.event_id → events.id
vx2 = 5.3
polyline([(g_events['x0'], 9.2), (vx2, 9.2), (vx2, 3.0), (g_favs['x1'], 3.0)])
one_mark(g_events['x0'], 9.2, horiz=True)     # || у левого края events
crow(g_favs['x1'], 3.0, 'right')               # foot наружу вправо от favorites

# ── Заголовок ─────────────────────────────────────────────────────────────
ax.text(9.0, 11.65,
        'Логическая схема базы данных SvoySport TV',
        ha='center', va='center', fontsize=12, fontweight='bold')

# ── Легенда (ниже всех таблиц) ────────────────────────────────────────────
lx, ly = 0.4, 1.60
ax.text(lx, ly, 'Условные обозначения:', fontsize=8, fontweight='bold')

d = 0.09
# || (один)
for dx in (0.0, -0.13):
    ax.plot([lx+0.45+dx, lx+0.45+dx], [ly-0.38-d, ly-0.38+d], 'k-', lw=0.9)
ax.plot([lx, lx+0.45], [ly-0.38, ly-0.38], 'k-', lw=0.9)
ax.text(lx+0.62, ly-0.38, '— один (PK-сторона связи)', fontsize=7.5, va='center')

# crow's foot (много)
for dy in (0.09, 0, -0.09):
    ax.plot([lx+0.45, lx+0.23], [ly-0.76, ly-0.76+dy], 'k-', lw=0.9)
ax.plot([lx, lx+0.45], [ly-0.76, ly-0.76], 'k-', lw=0.9)
ax.text(lx+0.62, ly-0.76, "— многие (crow's foot, FK-сторона)", fontsize=7.5, va='center')

# PK
ax.text(lx,      ly-1.14, 'PK', fontsize=7.5, fontweight='bold', va='center')
ax.text(lx+0.42, ly-1.14, 'field  — первичный ключ (курсив, подчёркнутый)',
        fontsize=7.5, style='italic', va='center')

# FK
ax.text(lx,      ly-1.48, 'FK', fontsize=7.5, fontweight='bold', va='center')
ax.text(lx+0.42, ly-1.48, 'field  — внешний ключ (курсив)',
        fontsize=7.5, style='italic', va='center')

plt.savefig(
    '/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/10_dbschema.png',
    dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 10_dbschema.png')
