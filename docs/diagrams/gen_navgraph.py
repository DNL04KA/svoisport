"""Граф навигации приложения (для раздела 5.2.2)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

fig, ax = plt.subplots(figsize=(16, 8))
ax.set_xlim(0, 16)
ax.set_ylim(0, 8)
ax.axis('off')
fig.patch.set_facecolor('white')

BORDER = '#1A3A5C'
TXT    = '#1A1A1A'

C_START  = '#D6E4F0'   # Splash — начало
C_MAIN   = '#D5E8D4'   # Home — главный
C_SUB    = '#FFF2CC'   # Второстепенные экраны
C_AUTH   = '#F8D7DA'   # Аутентификация
C_PLAYER = '#EDE7F6'   # Плеер

def screen(cx, cy, w, h, title, route, fc='#E8EEF4'):
    rect = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                          boxstyle='round,pad=0.18',
                          facecolor=fc, edgecolor=BORDER, linewidth=2.0, zorder=3)
    ax.add_patch(rect)
    ax.plot([cx - w/2 + 0.08, cx + w/2 - 0.08],
            [cy + h/2 - 0.42, cy + h/2 - 0.42],
            color=BORDER, lw=0.9, zorder=4)
    ax.text(cx, cy + h/2 - 0.21, title, ha='center', va='center',
            fontsize=9.5, fontweight='bold', color=TXT, zorder=5)
    ax.text(cx, cy - 0.05, route, ha='center', va='center',
            fontsize=7.5, color='#555555', style='italic', zorder=5)

def arrow(x1, y1, x2, y2, label='', rad=0.0, color=BORDER, lw=1.6):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                connectionstyle=f'arc3,rad={rad}'))
    if label:
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx, my + 0.22, label, ha='center', va='center',
                fontsize=7.5, color='#333333',
                bbox=dict(boxstyle='round,pad=0.12', facecolor='white',
                          edgecolor='none', alpha=0.9))

def back_arrow(x1, y1, x2, y2, rad=0.25):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color='#888888', lw=1.1,
                                linestyle='dashed',
                                connectionstyle=f'arc3,rad={rad}'))

# =================== TITLE ===================
ax.text(8.0, 7.72, 'Граф навигации приложения SvoySport TV',
        ha='center', va='center', fontsize=12, fontweight='bold', color=TXT)

# =================== NODES ===================
# SplashScreen
screen(2.0, 6.2, 2.8, 1.0, 'SplashScreen', 'splash', fc=C_START)

# HomeScreen (центральный)
screen(7.5, 6.2, 3.0, 1.0, 'HomeScreen', 'home', fc=C_MAIN)

# ScheduleScreen
screen(7.5, 4.2, 3.0, 1.0, 'ScheduleScreen', 'schedule', fc=C_SUB)

# AuthScreen
screen(3.5, 4.2, 3.0, 1.0, 'AuthScreen', 'auth', fc=C_AUTH)

# CodeVerificationScreen
screen(3.5, 2.2, 3.4, 1.1,
       'CodeVerificationScreen',
       'codeVerification/{email}', fc=C_AUTH)

# DetailsScreen
screen(12.0, 4.2, 3.0, 1.0, 'DetailsScreen', 'details/{matchId}', fc=C_SUB)

# PlayerScreen
screen(12.0, 2.2, 3.0, 1.0, 'PlayerScreen', 'player/{matchId}', fc=C_PLAYER)

# =================== ARROWS ===================
# Splash → Home
arrow(3.4, 6.2, 6.0, 6.2, label='автопереход (3 сек)')

# Home → Schedule
arrow(7.5, 5.7, 7.5, 4.72, label='Tab «Расписание»')

# Schedule → Home
back_arrow(7.1, 4.72, 7.1, 5.7, rad=0.3)

# Home → Auth
arrow(6.5, 5.75, 4.5, 4.72, label='Войти')

# Auth → CodeVerification
arrow(3.5, 3.70, 3.5, 2.77, label='email отправлен')

# CodeVerification → Home (успех)
arrow(5.2, 2.2, 6.0, 5.72, label='токен получен', color='#1E8449', rad=-0.2, lw=1.8)

# CodeVerification → Auth (Back)
back_arrow(3.9, 2.77, 3.9, 3.70, rad=0.35)

# Home → Details
arrow(9.0, 6.0, 10.5, 4.72, label='выбор матча')

# Details → Player
arrow(12.0, 3.70, 12.0, 2.77, label='«Смотреть сейчас»')

# Player → Back (home/details)
back_arrow(12.4, 2.77, 12.4, 3.70, rad=0.35)

# Details → Back
back_arrow(12.7, 4.72, 12.7, 5.72, rad=-0.3)

# ScheduleScreen → Details
arrow(8.85, 4.2, 10.5, 4.2, label='выбор матча')

# Start dot
ax.add_patch(plt.Circle((0.5, 6.2), 0.22, facecolor='black', zorder=4))
arrow(0.72, 6.2, 0.57, 6.2)  # крошечная стрелка от точки к splashregion
ax.annotate('', xy=(0.58, 6.2), xytext=(0.72, 6.2),
            arrowprops=dict(arrowstyle='->', color=BORDER, lw=1.6))

# =================== LEGEND ===================
lx, ly = 0.3, 2.8
legend_items = [
    (C_START,  'Экран запуска'),
    (C_MAIN,   'Главный экран'),
    (C_SUB,    'Навигационные экраны'),
    (C_AUTH,   'Аутентификация'),
    (C_PLAYER, 'Видеоплеер'),
]
ax.text(lx, ly, 'Условные обозначения:', fontsize=8, fontweight='bold')
for j, (fc, lbl) in enumerate(legend_items):
    ry = ly - 0.4 * (j + 1)
    rect = FancyBboxPatch((lx, ry - 0.13), 0.38, 0.28,
                          boxstyle='round,pad=0.04',
                          facecolor=fc, edgecolor=BORDER, linewidth=1.0)
    ax.add_patch(rect)
    ax.text(lx + 0.55, ry + 0.01, lbl, fontsize=7.5, va='center')

# Пунктирная стрелка = Back
ax.plot([lx, lx + 0.5], [ly - 2.35, ly - 2.35],
        color='#888888', lw=1.2, linestyle='dashed')
ax.annotate('', xy=(lx + 0.5, ly - 2.35), xytext=(lx + 0.45, ly - 2.35),
            arrowprops=dict(arrowstyle='->', color='#888888', lw=1.2))
ax.text(lx + 0.62, ly - 2.35, '— переход «Назад»',
        fontsize=7.5, va='center', color='#555555')

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/08_navgraph.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('Navigation graph saved → 08_navgraph.png')
