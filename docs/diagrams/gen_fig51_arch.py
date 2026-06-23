"""Рисунок 5.1 — Архитектура системы (академический стиль)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(18, 10))
ax.set_xlim(0, 18)
ax.set_ylim(0, 10)
ax.axis('off')
fig.patch.set_facecolor('white')

def box(cx, cy, w, h, text, fs=8.5, bold=False):
    rect = mpatches.FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                                   boxstyle='square,pad=0',
                                   facecolor='white', edgecolor='black',
                                   linewidth=1.2, zorder=3)
    ax.add_patch(rect)
    weight = 'bold' if bold else 'normal'
    ax.text(cx, cy, text, ha='center', va='center',
            fontsize=fs, fontweight=weight, color='black',
            zorder=4, wrap=True,
            multialignment='center')

def vline(x, y1, y2):
    ax.plot([x, x], [y1, y2], color='black', lw=1.0, zorder=2)

def hline(x1, x2, y):
    ax.plot([x1, x2], [y, y], color='black', lw=1.0, zorder=2)

def connect_down(px, py_bottom, cy_top):
    """Линия от низа родителя до горизонтальной перемычки потомков"""
    ax.plot([px, px], [py_bottom, cy_top], color='black', lw=1.0, zorder=2)

# ============== УРОВЕНЬ 0 — корень ==============
box(9.0, 9.2, 12.0, 0.75,
    'Система управления спортивными трансляциями SvoySport TV',
    fs=10, bold=True)

# ============== УРОВЕНЬ 1 — два раздела ==============
L1 = [
    (4.0,  7.6, 5.5, 0.65, 'Клиентская часть (Android TV)'),
    (14.0, 7.6, 5.5, 0.65, 'Серверная часть (Backend)'),
]
for (cx, cy, w, h, t) in L1:
    box(cx, cy, w, h, t, fs=9, bold=True)

# ствол от корня вниз
connect_down(9.0, 9.2 - 0.375, 7.93)
hline(4.0, 14.0, 7.93)
# ветки к L1
for (cx, cy, w, h, t) in L1:
    vline(cx, 7.93, cy + h/2)

# ============== УРОВЕНЬ 2 — слои клиента ==============
client_layers = [
    (1.6,  6.1, 2.8, 0.60, 'UI Layer\n(Presentation)'),
    (4.5,  6.1, 2.8, 0.60, 'ViewModel Layer\n(Business Logic)'),
    (7.4,  6.1, 2.8, 0.60, 'Data Layer\n(Repository)'),
    (4.5,  5.15, 2.8, 0.55, 'Локальний кеш /\nSharedPreferences'),
]
# горизонтальная перемычка клиента на y=6.42
hline(1.6, 7.4, 6.42)
connect_down(4.0, 7.6 - 0.325, 6.42)
for (cx, cy, w, h, t) in client_layers[:3]:
    vline(cx, 6.42, cy + h/2)
    box(cx, cy, w, h, t)
# кеш
vline(4.5, 6.1 - 0.30, 5.15 + 0.275)
box(4.5, 5.15, 2.8, 0.55, 'Локальный кэш /\nSharedPreferences')

# ============== УРОВЕНЬ 3 — компоненты UI ==============
ui_items = [
    (0.85, 4.8, 1.3, 0.50, 'HomeScreen'),
    (0.85, 4.15, 1.3, 0.50, 'DetailsScreen'),
    (0.85, 3.50, 1.3, 0.50, 'PlayerScreen'),
    (0.85, 2.85, 1.3, 0.50, 'AuthScreen'),
    (0.85, 2.20, 1.3, 0.50, 'ScheduleScreen'),
]
connect_down(1.6, 6.1 - 0.30, 4.8 + 0.25)
vline(1.6, 4.8 + 0.25, 2.20 + 0.25)
hline(0.85, 1.6, 4.8 + 0.25)
hline(0.85, 1.6, 4.15 + 0.25)
hline(0.85, 1.6, 3.50 + 0.25)
hline(0.85, 1.6, 2.85 + 0.25)
hline(0.85, 1.6, 2.20 + 0.25)
for item in ui_items:
    box(*item)

# ============== УРОВЕНЬ 3 — компоненты ViewModel ==============
vm_items = [
    (3.7, 4.8, 2.3, 0.50, 'HomeViewModel'),
    (3.7, 4.15, 2.3, 0.50, 'DetailsViewModel'),
    (3.7, 3.50, 2.3, 0.50, 'PlayerViewModel'),
    (3.7, 2.85, 2.3, 0.50, 'StateFlow / Events'),
]
connect_down(4.5, 6.1 - 0.30, 4.8 + 0.25)
vline(4.5, 4.8 + 0.25, 2.85 + 0.25)
for item in vm_items:
    hline(3.7, 4.5, item[1] + 0.25)
    box(*item)

# ============== УРОВЕНЬ 3 — компоненты Data ==============
data_items = [
    (6.55, 4.8, 2.7, 0.50, 'MatchRepository\n(интерфейс)'),
    (6.55, 4.15, 2.7, 0.50, 'ApiMatchRepository\n(Retrofit2 + OkHttp)'),
    (6.55, 3.50, 2.7, 0.50, 'MockMatchRepository\n(тестовые данные)'),
    (6.55, 2.85, 2.7, 0.50, 'JWT-токен / Coil кэш'),
]
connect_down(7.4, 6.1 - 0.30, 4.8 + 0.25)
vline(7.4, 4.8 + 0.25, 2.85 + 0.25)
for item in data_items:
    hline(6.55, 7.4, item[1] + 0.25)
    box(*item)

# ============== УРОВЕНЬ 2 — слои сервера ==============
srv_layers = [
    (11.5, 6.1, 3.0, 0.60, 'REST API Layer'),
    (14.5, 6.1, 3.0, 0.60, 'Business Logic'),
    (17.0, 6.1, 2.0, 0.60, 'Data Access\nLayer'),
]
hline(11.5, 17.0, 6.42)
connect_down(14.0, 7.6 - 0.325, 6.42)
for (cx, cy, w, h, t) in srv_layers:
    vline(cx, 6.42, cy + h/2)
    box(cx, cy, w, h, t)

# ============== УРОВЕНЬ 3 — компоненты сервера ==============
api_items = [
    (10.7, 4.8, 2.6, 0.50, 'GET /api/home'),
    (10.7, 4.15, 2.6, 0.50, 'GET /api/match/{id}'),
    (10.7, 3.50, 2.6, 0.50, 'POST /auth/send-code'),
    (10.7, 2.85, 2.6, 0.50, 'POST /auth/verify'),
]
connect_down(11.5, 6.1 - 0.30, 4.8 + 0.25)
vline(11.5, 4.8 + 0.25, 2.85 + 0.25)
for item in api_items:
    hline(10.7, 11.5, item[1] + 0.25)
    box(*item)

biz_items = [
    (13.7, 4.8, 2.6, 0.50, 'Агрегация расписания'),
    (13.7, 4.15, 2.6, 0.50, 'Генерация OTP-кодов'),
    (13.7, 3.50, 2.6, 0.50, 'Управление подписками'),
]
connect_down(14.5, 6.1 - 0.30, 4.8 + 0.25)
vline(14.5, 4.8 + 0.25, 3.50 + 0.25)
for item in biz_items:
    hline(13.7, 14.5, item[1] + 0.25)
    box(*item)

dal_items = [
    (16.15, 4.8, 2.7, 0.50, 'PostgreSQL'),
    (16.15, 4.15, 2.7, 0.50, 'Redis кэш'),
    (16.15, 3.50, 2.7, 0.50, 'HLS Scraper'),
    (16.15, 2.85, 2.7, 0.50, 'sport-tv.by / CDN'),
]
connect_down(17.0, 6.1 - 0.30, 4.8 + 0.25)
vline(17.0, 4.8 + 0.25, 2.85 + 0.25)
for item in dal_items:
    hline(16.15, 17.0, item[1] + 0.25)
    box(*item)

plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/07_architecture.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 07_architecture.png')
