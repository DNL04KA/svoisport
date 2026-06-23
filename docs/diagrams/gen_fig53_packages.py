"""Рисунок 5.3 — Структура пакетов проекта (академический стиль, 2 уровня)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(20, 8))
ax.set_xlim(0, 20)
ax.set_ylim(0, 8)
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
box(10, 7.42, 18.0, 0.80,
    'Структура пакетов проекта SvoySport TV  (com.svoysport.tv)',
    fs=11, bold=True)

# ── Уровень 1: пакеты ────────────────────────────────────────────────────
# 6 колонок, равномерно на ширине 20 (отступы 0.9 с каждой стороны)
L1_xs     = [1.8, 5.0, 8.2, 11.4, 14.6, 17.8]
L1_labels = ['data', 'di', 'navigation', 'ui', 'player /\nsession', 'utils']
L1_y, L1_w, L1_h = 6.1, 2.8, 0.75

for cx, lbl in zip(L1_xs, L1_labels):
    box(cx, L1_y, L1_w, L1_h, lbl, fs=9.5, bold=True)

# шина корень → L1
bus_y1 = 6.75
vl(10, 7.02, bus_y1)
hl(L1_xs[0], L1_xs[-1], bus_y1)
for cx in L1_xs:
    vl(cx, bus_y1, L1_y + L1_h / 2)

# ── Уровень 2: содержимое каждого пакета ─────────────────────────────────
L2_w, L2_h  = 2.6, 0.58
step        = 0.76
first_y     = 4.85
bus_offset  = 1.55   # от центра L1 до левой шины

# Для «ui» — подпакеты показаны как один блок с перечнем
L2_items = [
    (L1_xs[0], ['model', 'network', 'repository']),
    (L1_xs[1], ['AppModule', 'NetworkModule', 'RepositoryModule']),
    (L1_xs[2], ['AppNavGraph', 'Screen']),
    (L1_xs[3], ['screens', 'theme', 'components']),
    (L1_xs[4], ['PlayerManager', 'SessionManager']),
    (L1_xs[5], ['DateFormatter', 'Extensions']),
]

for cx, items in L2_items:
    bx      = cx - bus_offset
    fork_y  = L1_y - L1_h / 2 - 0.18
    last_y  = first_y - (len(items) - 1) * step

    vl(cx, L1_y - L1_h / 2, fork_y)
    hl(bx, cx, fork_y)
    vl(bx, fork_y, last_y)

    for k, txt in enumerate(items):
        cy = first_y - k * step
        hl(bx, cx - L2_w / 2, cy)
        box(cx, cy, L2_w, L2_h, txt, fs=8.5)

# ── Примечание под «ui/screens» ───────────────────────────────────────────
ui_cx = L1_xs[3]   # 11.4
note_y = first_y - 3 * step - 0.30   # под последним L2 элементом
ax.text(ui_cx, note_y,
        'screens: home · details · player\nauth · schedule · profile · splash',
        ha='center', va='center', fontsize=7.0,
        color='#555555', style='italic', zorder=4)

plt.savefig(
    '/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/09_packages.png',
    dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 09_packages.png')
