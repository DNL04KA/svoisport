"""IDEF0 Decomposition Diagram A0 — Диаграмма декомпозиции IDEF0"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch
import matplotlib.pyplot as plt

fig, ax = plt.subplots(figsize=(20, 14))
ax.set_xlim(0, 20)
ax.set_ylim(0, 14)
ax.axis('off')
fig.patch.set_facecolor('white')

# ---- helpers ----
def box(ax, cx, cy, w, h, num, title, fcolor='#D6E4F0', ecolor='#1A3A5C'):
    r = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                       boxstyle='square,pad=0',
                       facecolor=fcolor, edgecolor=ecolor, linewidth=2.0, zorder=3)
    ax.add_patch(r)
    # number in bottom-right
    ax.text(cx + w/2 - 0.12, cy - h/2 + 0.12, num, ha='right', va='bottom',
            fontsize=8, color='#555555', style='italic', zorder=4)
    # title
    lines = title.split('\n')
    for i, line in enumerate(lines):
        offset = (len(lines) - 1) / 2 * 0.22 - i * 0.28
        ax.text(cx, cy + offset + 0.05, line, ha='center', va='center',
                fontsize=9.5, fontweight='bold', zorder=4)

def harrow(ax, x1, x2, y, label='', label_y_off=0.15, color='#1A3A5C', lw=1.6,
           fontsize=8.0):
    ax.annotate('', xy=(x2, y), xytext=(x1, y),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw, mutation_scale=14))
    if label:
        ax.text((x1 + x2) / 2, y + label_y_off, label, ha='center', va='bottom',
                fontsize=fontsize, color='#222222',
                bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                          edgecolor='none', alpha=0.9), zorder=5)

def varrow(ax, x, y1, y2, label='', label_x_off=0.15, color='#1A3A5C', lw=1.6,
           fontsize=8.0, mech=False):
    arrowstyle = '->' if not mech else '->'
    clr = color if not mech else '#555555'
    ax.annotate('', xy=(x, y2), xytext=(x, y1),
                arrowprops=dict(arrowstyle=arrowstyle, color=clr, lw=lw,
                                mutation_scale=14))
    if label:
        ax.text(x + label_x_off, (y1 + y2) / 2, label, ha='left', va='center',
                fontsize=fontsize, color='#222222',
                bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                          edgecolor='none', alpha=0.9), zorder=5)

def dot(ax, x, y, color='#1A3A5C'):
    ax.plot(x, y, 'o', color=color, ms=4, zorder=6)

def seg(ax, x1, y1, x2, y2, color='#1A3A5C', lw=1.6):
    ax.plot([x1, x2], [y1, y2], color=color, lw=lw, zorder=5)

# ===================== TITLE =====================
ax.text(10.0, 13.7, 'Диаграмма IDEF0. Уровень A0 — Декомпозиция',
        ha='center', va='top', fontsize=13, fontweight='bold')
ax.text(10.0, 13.25, 'Система: «СвойСпорт TV»  |  Родительская функция: A-0  |  Дата: 2026-05-24',
        ha='center', va='top', fontsize=8.5, color='#555555')

# ===================== FUNCTION BOXES =====================
# A1 — Аутентификация пользователя
box(ax,  4.5, 10.5, 4.8, 2.0, 'A1',
    'Аутентификация\nпользователя',
    fcolor='#E8EEF4')

# A2 — Загрузка каталога
box(ax, 10.5, 10.5, 4.8, 2.0, 'A2',
    'Загрузка каталога\nтрансляций',
    fcolor='#E8EEF4')

# A3 — Просмотр трансляции
box(ax,  4.5,  6.5, 4.8, 2.0, 'A3',
    'Просмотр\nтрансляции',
    fcolor='#E8EEF4')

# A4 — Регистрация сеанса
box(ax, 10.5,  6.5, 4.8, 2.0, 'A4',
    'Регистрация сеанса\nпросмотра',
    fcolor='#E8EEF4')

# A5 — Обработка ошибок
box(ax, 16.0,  6.5, 4.0, 2.0, 'A5',
    'Обработка ошибок\nи уведомлений',
    fcolor='#E8EEF4')

# ===================== LEFT BOUNDARY — INPUTS =====================
# I1: Запросы пользователя → A1
harrow(ax, 0.5, 2.1, 'I1: Данные для\nаутентификации (email/код)', color='#1A3A5C')
# I2: Запросы пользователя → A2
harrow(ax, 0.5, 10.8, '', color='#1A3A5C')
ax.annotate('', xy=(8.1, 10.8), xytext=(0.5, 10.8),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(4.5, 10.95, 'I1: Запрос каталога', ha='center', va='bottom', fontsize=8.0,
        bbox=dict(boxstyle='round,pad=0.15', facecolor='white', edgecolor='none', alpha=0.9), zorder=5)

# I3: команды управления → A3
ax.annotate('', xy=(2.1, 6.7), xytext=(0.5, 6.7),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(1.3, 6.85, 'I2: Команды\nуправления', ha='center', va='bottom', fontsize=8.0,
        bbox=dict(boxstyle='round,pad=0.15', facecolor='white', edgecolor='none', alpha=0.9), zorder=5)

# ===================== INTERNAL ARROWS =====================

# A1 → A2: токен доступа
harrow(ax, 6.9, 13.9, 10.5, 10.8,
       'Токен\nдоступа', color='#1A3A5C')
# connect A1 output to pipe
seg(ax, 6.9, 10.8, 8.1, 10.8)
# A1 vert out
seg(ax, 6.9, 11.5, 6.9, 12.2)
ax.annotate('', xy=(6.9, 12.2), xytext=(6.9, 13.9),
            arrowprops=dict(arrowstyle='-', color='#1A3A5C', lw=1.6))

# A2 → A3: URL потока
seg(ax, 10.5, 9.5, 10.5, 8.0)
seg(ax, 10.5, 8.0, 6.9, 8.0)
ax.annotate('', xy=(6.9, 7.5), xytext=(6.9, 8.0),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(9.0, 8.15, 'URL потока', ha='center', va='bottom', fontsize=8.0,
        bbox=dict(boxstyle='round,pad=0.15', facecolor='white', edgecolor='none', alpha=0.9), zorder=5)

# A3 → A4: данные сеанса
harrow(ax, 6.9, 6.3, 8.1, 6.3,
       'Данные сеанса\n(matchId, время)', color='#1A3A5C')

# A3 → A5: ошибки плеера
seg(ax, 6.9, 5.5, 6.9, 4.8)
seg(ax, 6.9, 4.8, 16.0, 4.8)
ax.annotate('', xy=(16.0, 5.5), xytext=(16.0, 4.8),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(11.5, 4.95, 'Ошибки воспроизведения', ha='center', va='bottom', fontsize=8.0,
        bbox=dict(boxstyle='round,pad=0.15', facecolor='white', edgecolor='none', alpha=0.9), zorder=5)

# A5 → A3: команда повтора
seg(ax, 14.0, 6.5, 14.0, 3.8)
seg(ax, 14.0, 3.8, 4.1, 3.8)
ax.annotate('', xy=(4.1, 5.5), xytext=(4.1, 3.8),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(9.0, 3.95, 'Команда повтора / отмены', ha='center', va='bottom', fontsize=8.0,
        bbox=dict(boxstyle='round,pad=0.15', facecolor='white', edgecolor='none', alpha=0.9), zorder=5)

dot(ax, 6.9, 4.8)
dot(ax, 14.0, 4.8)
dot(ax, 14.0, 6.5)

# ===================== RIGHT BOUNDARY — OUTPUTS =====================
# A2 output → right (список трансляций)
seg(ax, 12.9, 10.8, 19.5, 10.8)
ax.annotate('', xy=(19.5, 10.8), xytext=(12.9, 10.8),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(16.5, 10.95, 'O1: Список трансляций\n(каталог)', ha='center', va='bottom',
        fontsize=8.0, bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                                edgecolor='none', alpha=0.9), zorder=5)

# A3 output → right (видеопоток)
seg(ax, 6.9, 6.5, 6.9, 7.4)  # already done for A2→A3
seg(ax, 6.9, 5.5, 19.5, 5.5)  # just video output placeholder
ax.annotate('', xy=(19.5, 5.5), xytext=(6.9, 5.5),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(13.5, 5.65, 'O2: Видеопоток на экране', ha='center', va='bottom',
        fontsize=8.0, bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                                edgecolor='none', alpha=0.9), zorder=5)

# A4 output
seg(ax, 12.9, 6.3, 19.5, 6.3)
ax.annotate('', xy=(19.5, 6.3), xytext=(12.9, 6.3),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
ax.text(16.5, 6.45, 'O3: История просмотра', ha='center', va='bottom',
        fontsize=8.0, bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                                edgecolor='none', alpha=0.9), zorder=5)

# A5 output
seg(ax, 18.0, 6.5, 19.5, 6.5)
ax.annotate('', xy=(19.5, 7.8), xytext=(18.0, 7.8),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.6, mutation_scale=14))
seg(ax, 18.0, 6.5, 18.0, 7.8)
ax.text(18.75, 7.95, 'O4: Уведомления\nоб ошибках', ha='center', va='bottom',
        fontsize=8.0, bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                                edgecolor='none', alpha=0.9), zorder=5)

# ===================== TOP BOUNDARY — CONTROLS =====================
# C1 → A1
varrow(ax, 4.5, 13.0, 11.5, 'C1: Требования аутентификации', label_x_off=0.15,
       color='#1A3A5C')
# C2 → A2
varrow(ax, 10.5, 13.0, 11.5, 'C2: Правила доступа к контенту', label_x_off=0.15,
       color='#1A3A5C')
# C3 → A3
varrow(ax, 4.5, 9.0, 7.5, 'C3: Протоколы потоковой передачи\n(HLS/DASH)', label_x_off=0.15,
       color='#1A3A5C')

seg(ax, 4.5, 13.0, 10.5, 13.0)
dot(ax, 4.5, 13.0)
dot(ax, 10.5, 13.0)

# ===================== BOTTOM BOUNDARY — MECHANISMS =====================
# M1 → A1
seg(ax, 2.8, 1.5, 2.8, 9.5)
ax.annotate('', xy=(2.8, 9.5), xytext=(2.8, 1.5),
            arrowprops=dict(arrowstyle='->', color='#555555', lw=1.4, mutation_scale=14))
ax.text(2.0, 1.2, 'M1: SportTV API\n(auth endpoint)', ha='center', va='top',
        fontsize=8.0, color='#444444')

# M2 → A2
seg(ax, 9.0, 1.5, 9.0, 9.5)
ax.annotate('', xy=(9.0, 9.5), xytext=(9.0, 1.5),
            arrowprops=dict(arrowstyle='->', color='#555555', lw=1.4, mutation_scale=14))
ax.text(9.0, 1.2, 'M2: Сервер трансляций\n(SportTV API)', ha='center', va='top',
        fontsize=8.0, color='#444444')

# M3 → A3
seg(ax, 4.5, 1.5, 4.5, 5.5)
ax.annotate('', xy=(4.5, 5.5), xytext=(4.5, 1.5),
            arrowprops=dict(arrowstyle='->', color='#555555', lw=1.4, mutation_scale=14))
ax.text(4.5, 1.2, 'M3: Android TV OS\n+ ExoPlayer', ha='center', va='top',
        fontsize=8.0, color='#444444')

# M4 → A4
seg(ax, 12.0, 1.5, 12.0, 5.5)
ax.annotate('', xy=(12.0, 5.5), xytext=(12.0, 1.5),
            arrowprops=dict(arrowstyle='->', color='#555555', lw=1.4, mutation_scale=14))
ax.text(12.0, 1.2, 'M4: База данных\nсеансов', ha='center', va='top',
        fontsize=8.0, color='#444444')

# ===================== FRAME =====================
outer = FancyBboxPatch((0.1, 0.1), 19.8, 13.5, boxstyle='square,pad=0',
                       facecolor='none', edgecolor='#333333', linewidth=2.0, zorder=0)
ax.add_patch(outer)

plt.tight_layout(pad=0.3)
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/04_idef0_decomp.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('IDEF0 decomposition diagram saved.')
