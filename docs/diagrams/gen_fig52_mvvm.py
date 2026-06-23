"""Рисунок 5.2 — MVVM + StateFlow (дерево, академический стиль)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(15, 10))
ax.set_xlim(0, 15)
ax.set_ylim(0, 10)
ax.axis('off')
fig.patch.set_facecolor('white')

def box(cx, cy, w, h, text, fs=8.5, bold=False):
    ax.add_patch(mpatches.FancyBboxPatch(
        (cx - w/2, cy - h/2), w, h,
        boxstyle='square,pad=0',
        facecolor='white', edgecolor='black', linewidth=1.2, zorder=3))
    ax.text(cx, cy, text, ha='center', va='center',
            fontsize=fs, fontweight='bold' if bold else 'normal',
            color='black', zorder=4, multialignment='center')

def vl(x, y1, y2): ax.plot([x, x], [y1, y2], color='black', lw=1.0, zorder=2)
def hl(x1, x2, y): ax.plot([x1, x2], [y, y], color='black', lw=1.0, zorder=2)

# ── Заголовок ──────────────────────────────────────────────────────────────
box(7.5, 9.45, 14.5, 0.75,
    'Схема MVVM с однонаправленным потоком данных (StateFlow)',
    fs=11, bold=True)

# ── Уровень 1: три слоя ───────────────────────────────────────────────────
L1_xs     = [2.5, 7.5, 12.5]
L1_labels = [
    'UI Layer\n(Presentation)',
    'ViewModel Layer\n(Business Logic)',
    'Data Layer\n(Данные)',
]
L1_y, L1_w, L1_h = 8.1, 4.2, 0.80

for cx, lbl in zip(L1_xs, L1_labels):
    box(cx, L1_y, L1_w, L1_h, lbl, fs=9.5, bold=True)

# Шина L0 → L1
bus_y1 = 8.82          # между низом заголовка и верхом L1
vl(7.5, 9.075, bus_y1)
hl(L1_xs[0], L1_xs[-1], bus_y1)
for cx in L1_xs:
    vl(cx, bus_y1, L1_y + L1_h / 2)

# ── Уровень 2: компоненты каждого слоя ────────────────────────────────────
L2_w, L2_h  = 3.8, 0.58
step        = 0.75
first_y     = 6.85
bus_offset  = 2.1      # расстояние от центра колонки до левой шины

L2_items = [
    (L1_xs[0], [
        'HomeScreen',
        'DetailsScreen',
        'PlayerScreen',
        'ScheduleScreen',
        'AuthScreen / SplashScreen',
    ]),
    (L1_xs[1], [
        'HomeViewModel',
        'DetailsViewModel',
        'PlayerViewModel',
        'AuthViewModel',
    ]),
    (L1_xs[2], [
        'MatchRepository\n(интерфейс)',
        'ApiMatchRepository\n(Retrofit2 + OkHttp)',
        'MockMatchRepository\n(тестовые данные)',
        'SessionManager',
    ]),
]

for cx, items in L2_items:
    bx      = cx - bus_offset
    fork_y  = L1_y - L1_h / 2 - 0.15
    last_y  = first_y - (len(items) - 1) * step

    vl(cx, L1_y - L1_h / 2, fork_y)   # ствол от L1 вниз
    hl(bx, cx, fork_y)                 # горизонталь к шине
    vl(bx, fork_y, last_y)             # вертикальная шина

    for k, txt in enumerate(items):
        cy = first_y - k * step
        hl(bx, cx - L2_w / 2, cy)     # засечка к боксу
        box(cx, cy, L2_w, L2_h, txt, fs=8.5)

plt.savefig(
    '/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/06_mvvm_flow.png',
    dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 06_mvvm_flow.png')
