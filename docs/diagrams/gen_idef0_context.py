"""IDEF0 Context Diagram A-0 — Контекстная диаграмма IDEF0 (ГОСТ Р ИСО/МЭК 10746)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import matplotlib.patheffects as pe

fig, ax = plt.subplots(figsize=(18, 13))
ax.set_xlim(0, 18)
ax.set_ylim(0, 13)
ax.axis('off')
fig.patch.set_facecolor('white')

# ---- helpers ----
def box(ax, cx, cy, w, h, title, sub='', fcolor='#D6E4F0', ecolor='#1A3A5C'):
    r = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                       boxstyle='square,pad=0',
                       facecolor=fcolor, edgecolor=ecolor, linewidth=2.5, zorder=3)
    ax.add_patch(r)
    ax.text(cx, cy + 0.12, title, ha='center', va='center',
            fontsize=12, fontweight='bold', zorder=4)
    if sub:
        ax.text(cx, cy - 0.35, sub, ha='center', va='center',
                fontsize=8.5, color='#444444', zorder=4)

def arr(ax, x1, y1, x2, y2, lw=1.8, color='#1A3A5C'):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                mutation_scale=18))

def label(ax, x, y, text, ha='center', va='center', fontsize=9.5, bold=False,
          bbox_fc='white'):
    fw = 'bold' if bold else 'normal'
    ax.text(x, y, text, ha=ha, va=va, fontsize=fontsize, fontweight=fw,
            bbox=dict(boxstyle='round,pad=0.25', facecolor=bbox_fc,
                      edgecolor='none', alpha=0.9), zorder=5)

# ===================== BORDER & TITLE =====================
outer = FancyBboxPatch((0.2, 0.4), 17.6, 12.2, boxstyle='square,pad=0',
                       facecolor='white', edgecolor='#333333', linewidth=2.0, zorder=0)
ax.add_patch(outer)

ax.text(9.0, 12.85, 'Диаграмма IDEF0. Уровень A-0 (Контекстная)',
        ha='center', va='top', fontsize=13, fontweight='bold')
ax.text(9.0, 12.35, 'Система: Приложение «СвойСпорт TV»  |  Дата: 2026-05-24  |  Версия: 1.0',
        ha='center', va='top', fontsize=8.5, color='#555555')

# ===================== MAIN FUNCTION BOX =====================
box(ax, 9.0, 6.5, 6.0, 3.8,
    'A-0',
    'Обеспечить просмотр\nспортивных трансляций\nна Smart TV устройстве',
    fcolor='#E8EEF4', ecolor='#1A3A5C')
# Function number tag
ax.text(6.22, 4.72, 'A-0', ha='left', va='bottom', fontsize=8,
        color='#555555', style='italic')

# ===================== INPUTS (LEFT) =====================
# I1
arr(ax, 2.5, 7.8, 6.0, 7.4)
label(ax, 2.2, 8.05, 'I1: Запросы\nпользователя\n(навигация,\nвыбор трансляции)', ha='center', fontsize=8.5)

# I2
arr(ax, 2.5, 5.9, 6.0, 6.3)
label(ax, 2.2, 5.65, 'I2: Команды\nуправления плеером\n(пауза, перемотка,\nвыход)', ha='center', fontsize=8.5)

# I3
arr(ax, 2.5, 6.5, 6.0, 6.5)
# connector dot
ax.plot(2.5, 7.8, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(2.5, 6.5, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(2.5, 5.9, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot([2.5, 2.5], [5.9, 7.8], color='#1A3A5C', lw=1.8, zorder=5)
ax.plot([1.5, 2.5], [6.5, 6.5], color='#1A3A5C', lw=1.8, zorder=5)
ax.annotate('', xy=(1.5, 6.5), xytext=(0.8, 6.5),
            arrowprops=dict(arrowstyle='-', color='#1A3A5C', lw=1.8))
label(ax, 0.5, 6.5, 'Вход', ha='center', fontsize=8, bold=True, bbox_fc='#EEF4FF')

# ===================== OUTPUTS (RIGHT) =====================
# O1
arr(ax, 12.0, 7.5, 15.5, 7.8)
label(ax, 15.8, 7.8, 'O1: Видеопоток\nна экране\n(HLS/DASH)', ha='center', fontsize=8.5)

# O2
arr(ax, 12.0, 6.5, 15.5, 6.5)
label(ax, 15.85, 6.5, 'O2: Интерфейс\nкаталога и\nнавигации', ha='center', fontsize=8.5)

# O3
arr(ax, 12.0, 5.5, 15.5, 5.2)
label(ax, 15.85, 5.1, 'O3: Уведомления\nоб ошибках и\nстатусах', ha='center', fontsize=8.5)

ax.plot([15.5, 15.5], [5.2, 7.8], color='#1A3A5C', lw=1.8, zorder=5)
ax.plot(15.5, 5.2, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(15.5, 6.5, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(15.5, 7.8, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot([15.5, 16.2], [6.5, 6.5], color='#1A3A5C', lw=1.8, zorder=5)
label(ax, 16.6, 6.5, 'Выход', ha='center', fontsize=8, bold=True, bbox_fc='#EEF4FF')

# ===================== CONTROLS (TOP) =====================
# C1
arr(ax, 7.0, 10.3, 7.8, 8.3)
label(ax, 6.8, 10.65, 'C1: Требования к качеству\nвидео (битрейт, разрешение)', ha='center', fontsize=8.5)

# C2
arr(ax, 9.0, 10.5, 9.0, 8.3)
label(ax, 9.0, 10.85, 'C2: Правила доступа\nк трансляциям', ha='center', fontsize=8.5)

# C3
arr(ax, 11.0, 10.3, 10.2, 8.3)
label(ax, 11.2, 10.65, 'C3: Протоколы\nHLS / DASH', ha='center', fontsize=8.5)

ax.plot([7.0, 11.0], [10.3, 10.3], color='#1A3A5C', lw=1.8, zorder=5)
ax.plot(7.0, 10.3, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(9.0, 10.3, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot(11.0, 10.3, 'o', color='#1A3A5C', ms=4, zorder=6)
ax.plot([9.0, 9.0], [10.3, 11.0], color='#1A3A5C', lw=1.8, zorder=5)
ax.plot([9.0, 9.0], [11.0, 11.5], color='#1A3A5C', lw=1.8, zorder=5)
label(ax, 9.0, 11.8, 'Управление', ha='center', fontsize=8, bold=True, bbox_fc='#EEF4FF')

# ===================== MECHANISMS (BOTTOM) =====================
# M1
arr(ax, 7.5, 4.6, 7.8, 5.5, color='#444444')
ax.plot([7.5, 7.5], [4.6, 3.9], color='#444444', lw=1.8)
label(ax, 7.5, 3.55, 'M1: Android TV OS\n+ ExoPlayer', ha='center', fontsize=8.5)

# M2
arr(ax, 9.0, 4.6, 9.0, 5.5, color='#444444')
ax.plot([9.0, 9.0], [4.6, 3.9], color='#444444', lw=1.8)
label(ax, 9.0, 3.55, 'M2: Сервер\nтрансляций\n(SportTV API)', ha='center', fontsize=8.5)

# M3
arr(ax, 10.5, 4.6, 10.2, 5.5, color='#444444')
ax.plot([10.5, 10.5], [4.6, 3.9], color='#444444', lw=1.8)
label(ax, 10.5, 3.55, 'M3: Smart TV\nустройство\n(аппаратная часть)', ha='center', fontsize=8.5)

ax.plot([7.5, 10.5], [4.6, 4.6], color='#444444', lw=1.8, zorder=5)
ax.plot(7.5, 4.6, 'o', color='#444444', ms=4, zorder=6)
ax.plot(9.0, 4.6, 'o', color='#444444', ms=4, zorder=6)
ax.plot(10.5, 4.6, 'o', color='#444444', ms=4, zorder=6)
ax.plot([9.0, 9.0], [4.6, 2.9], color='#444444', lw=1.8, zorder=5)
label(ax, 9.0, 2.6, 'Механизмы', ha='center', fontsize=8, bold=True, bbox_fc='#EEF4FF')

# ===================== IDEF0 FRAME =====================
frame = FancyBboxPatch((0.3, 0.5), 17.4, 1.8, boxstyle='square,pad=0',
                       facecolor='#F5F5F5', edgecolor='#888888', linewidth=1.0, zorder=0)
ax.add_patch(frame)
ax.plot([0.3, 17.7], [0.5, 0.5], color='#888888', lw=1.0)
ax.plot([0.3, 17.7], [2.3, 2.3], color='#888888', lw=1.0)
ax.text(0.5, 2.1, 'Автор: СвойСпорт команда', fontsize=7.5, va='top')
ax.text(0.5, 1.75, 'Проект: ProgramOnTV', fontsize=7.5, va='top')
ax.text(0.5, 1.4, 'Диаграмма: A-0 Контекстная', fontsize=7.5, va='top')
ax.text(0.5, 1.05, 'Дата: 2026-05-24', fontsize=7.5, va='top')
ax.text(9.0, 2.1, 'ГОСТ Р ИСО/МЭК 10746  •  Нотация IDEF0', ha='center',
        fontsize=8, va='top', color='#555555')

plt.tight_layout(pad=0.3)
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/03_idef0_context.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('IDEF0 context diagram saved.')
