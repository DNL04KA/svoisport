"""Рисунок 5.4 — Граф навигации приложения (академический стиль)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(16, 9))
ax.set_xlim(0, 16)
ax.set_ylim(0, 9)
ax.axis('off')
fig.patch.set_facecolor('white')

def box(cx, cy, w, h, title, route='', fs=8.5):
    ax.add_patch(mpatches.FancyBboxPatch(
        (cx - w/2, cy - h/2), w, h,
        boxstyle='square,pad=0',
        facecolor='white', edgecolor='black', linewidth=1.3, zorder=3))
    if route:
        # горизонтальная линия под заголовком
        ax.plot([cx - w/2, cx + w/2],
                [cy + h/2 - 0.40, cy + h/2 - 0.40],
                color='black', lw=0.8, zorder=4)
        ax.text(cx, cy + h/2 - 0.20, title, ha='center', va='center',
                fontsize=fs, fontweight='bold', color='black', zorder=5)
        ax.text(cx, cy - 0.08, route, ha='center', va='center',
                fontsize=7.0, color='#444444', style='italic', zorder=5)
    else:
        ax.text(cx, cy, title, ha='center', va='center',
                fontsize=fs, fontweight='bold', color='black', zorder=5)

def arr(x1, y1, x2, y2, label='', rad=0.0, dashed=False, lw=1.2):
    ls = 'dashed' if dashed else 'solid'
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color='black', lw=lw,
                                linestyle=ls,
                                connectionstyle=f'arc3,rad={rad}'))
    if label:
        mx, my = (x1 + x2) / 2, (y1 + y2) / 2
        off = 0.24
        ax.text(mx, my + off, label, ha='center', va='center', fontsize=7.0,
                bbox=dict(boxstyle='round,pad=0.10', facecolor='white',
                          edgecolor='none', alpha=0.95))

BW, BH = 2.6, 0.90   # ширина и высота блока

# ── Заголовок ──────────────────────────────────────────────────────────────
box(8.0, 8.55, 14.0, 0.65,
    'Граф навигации приложения SvoySport TV', fs=10)

# ── Экраны ────────────────────────────────────────────────────────────────
# Стартовая точка
ax.add_patch(plt.Circle((0.42, 7.1), 0.22, facecolor='black', zorder=4))

NODES = {
    'splash':  (2.10, 7.10, 'SplashScreen',          'splash'),
    'home':    (5.70, 7.10, 'HomeScreen',             'home'),
    'auth':    (2.10, 5.10, 'AuthScreen',             'auth'),
    'schedule':(5.70, 5.10, 'ScheduleScreen',         'schedule'),
    'details': (9.30, 5.10, 'DetailsScreen',          'details/{matchId}'),
    'code':    (2.10, 3.10, 'CodeVerificationScreen', 'codeVerification/{email}'),
    'player':  (9.30, 3.10, 'PlayerScreen',           'player/{matchId}'),
    'profile': (13.20, 7.10, 'ProfileScreen',         'profile'),
    'sub':     (13.20, 5.10, 'SubscriptionScreen',    'subscription'),
    'settings':(13.20, 3.10, 'SettingsScreen',        'settings'),
}

for key, (cx, cy, title, route) in NODES.items():
    box(cx, cy, BW, BH, title, route)

# ── Стрелки ────────────────────────────────────────────────────────────────
half  = BH / 2
halfw = BW / 2

# Старт → Splash
ax.annotate('', xy=(2.10 - halfw, 7.10), xytext=(0.64, 7.10),
            arrowprops=dict(arrowstyle='->', color='black', lw=1.2))

# Splash → Home
arr(2.10 + halfw, 7.10, 5.70 - halfw, 7.10, label='автопереход (3 сек)')

# Home → Schedule  (вниз)
arr(5.70, 7.10 - half, 5.70, 5.10 + half, label='Расписание')
# Schedule → Home  (назад, пунктир, смещение вправо)
arr(5.90, 5.10 + half, 5.90, 7.10 - half, dashed=True, rad=-0.25)

# Home → Auth  (вниз-влево)
arr(5.70 - halfw, 6.80, 2.10 + halfw, 5.40, label='Войти')
# Auth → CodeVerif  (вниз)
arr(2.10, 5.10 - half, 2.10, 3.10 + half, label='email отправлен')
# CodeVerif → Auth  (назад, пунктир)
arr(2.35, 3.10 + half, 2.35, 5.10 - half, dashed=True, rad=-0.3)
# CodeVerif → Home  (вверх-вправо, токен)
arr(2.10 + halfw, 3.55, 5.70 - halfw, 6.70,
    label='токен получен', rad=-0.18)

# Home → Details  (вниз-вправо)
arr(5.70 + halfw, 6.75, 9.30 - halfw, 5.40, label='выбор матча')
# Schedule → Details  (вправо)
arr(5.70 + halfw, 5.10, 9.30 - halfw, 5.10, label='выбор матча')
# Details → Player  (вниз)
arr(9.30, 5.10 - half, 9.30, 3.10 + half, label='Смотреть сейчас')
# Player → Details  (назад, пунктир)
arr(9.55, 3.10 + half, 9.55, 5.10 - half, dashed=True, rad=-0.3)
# Details → Home  (назад, пунктир, дуга влево)
arr(9.30 - halfw, 5.55, 5.70 + halfw, 6.75, dashed=True, rad=0.25)

# Home → Profile  (вправо)
arr(5.70 + halfw, 7.10, 13.20 - halfw, 7.10, label='Профиль')
# Profile → Sub  (вниз)
arr(13.20, 7.10 - half, 13.20, 5.10 + half, label='Подписка')
# Sub → Profile  (назад, пунктир)
arr(13.48, 5.10 + half, 13.48, 7.10 - half, dashed=True, rad=-0.25)
# Sub → Settings  (вниз, цепочка)
arr(13.20, 5.10 - half, 13.20, 3.10 + half, label='Настройки')
# Settings → Sub  (назад, пунктир)
arr(13.48, 3.10 + half, 13.48, 5.10 - half, dashed=True, rad=-0.25)

# ── Легенда ───────────────────────────────────────────────────────────────
lx, ly = 0.25, 2.30
ax.text(lx, ly, 'Условные обозначения:', fontsize=8, fontweight='bold')

# Сплошная стрелка
ax.annotate('', xy=(lx + 0.75, ly - 0.42), xytext=(lx, ly - 0.42),
            arrowprops=dict(arrowstyle='->', color='black', lw=1.2))
ax.text(lx + 0.90, ly - 0.42, '— переход между экранами',
        fontsize=7.5, va='center')

# Пунктирная стрелка
ax.annotate('', xy=(lx + 0.75, ly - 0.84), xytext=(lx, ly - 0.84),
            arrowprops=dict(arrowstyle='->', color='black', lw=1.2,
                            linestyle='dashed'))
ax.text(lx + 0.90, ly - 0.84, '— переход «Назад»',
        fontsize=7.5, va='center')

# Начальная точка
ax.add_patch(plt.Circle((lx + 0.20, ly - 1.26), 0.14,
             facecolor='black', zorder=4))
ax.text(lx + 0.90, ly - 1.26, '— начальная точка приложения',
        fontsize=7.5, va='center')

plt.savefig(
    '/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/08_navgraph.png',
    dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 08_navgraph.png')
