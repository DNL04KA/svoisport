"""Use Case Diagram — Диаграмма вариантов использования (ГОСТ 19.701-90)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, Ellipse, FancyArrowPatch
import numpy as np

fig, ax = plt.subplots(1, 1, figsize=(16, 20))
ax.set_xlim(0, 16)
ax.set_ylim(0, 20)
ax.set_aspect('equal')
ax.axis('off')

fig.patch.set_facecolor('white')

# ---------- helpers ----------
def actor(ax, x, y, label, fontsize=9):
    # head
    ax.add_patch(plt.Circle((x, y + 1.0), 0.35, fill=True, facecolor='white',
                             edgecolor='black', linewidth=1.5, zorder=3))
    # body
    ax.plot([x, x], [y + 0.65, y + 0.0], color='black', lw=1.5, zorder=3)
    # arms
    ax.plot([x - 0.45, x + 0.45], [y + 0.38, y + 0.38], color='black', lw=1.5, zorder=3)
    # left leg
    ax.plot([x, x - 0.4], [y + 0.0, y - 0.55], color='black', lw=1.5, zorder=3)
    # right leg
    ax.plot([x, x + 0.4], [y + 0.0, y - 0.55], color='black', lw=1.5, zorder=3)
    ax.text(x, y - 0.85, label, ha='center', va='top', fontsize=fontsize,
            fontweight='bold', wrap=True)

def usecase(ax, cx, cy, w, h, text, fontsize=8.5):
    ell = Ellipse((cx, cy), width=w, height=h, fill=True,
                  facecolor='#F5F5F5', edgecolor='black', linewidth=1.5, zorder=2)
    ax.add_patch(ell)
    # wrap text manually
    words = text.split('\n')
    for i, line in enumerate(words):
        offset = (len(words) - 1) / 2 * 0.22
        ax.text(cx, cy + offset - i * 0.27, line, ha='center', va='center',
                fontsize=fontsize, zorder=3)

def system_box(ax, x, y, w, h, title):
    rect = FancyBboxPatch((x, y), w, h, boxstyle='square,pad=0',
                           fill=False, edgecolor='black', linewidth=1.5, zorder=1)
    ax.add_patch(rect)
    ax.text(x + w/2, y + h + 0.12, title, ha='center', va='bottom',
            fontsize=10, fontweight='bold')

def arrow(ax, x1, y1, x2, y2):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color='black', lw=1.2))

def line(ax, x1, y1, x2, y2):
    ax.plot([x1, x2], [y1, y2], color='black', lw=1.0, zorder=2)

def dashed_arrow(ax, x1, y1, x2, y2, label=''):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color='#444444', lw=1.0,
                                linestyle='dashed'))
    if label:
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx + 0.1, my, label, fontsize=7, color='#333333', style='italic')

# ===================== TITLE =====================
ax.text(8, 19.5, 'Диаграмма вариантов использования\nСистема «СвойСпорт TV»',
        ha='center', va='top', fontsize=12, fontweight='bold')

# ===================== SYSTEM BOUNDARY =====================
system_box(ax, 3.2, 1.0, 9.6, 17.2,
           'Приложение «СвойСпорт TV»')

# ===================== ACTORS =====================
# Зритель — left
actor(ax, 1.3, 9.5, 'Зритель')

# Администратор — right
actor(ax, 14.7, 15.5, 'Адми-\nнистратор')

# ===================== USE CASES — Зритель =====================
uc_viewer = [
    (7.0, 17.2, 3.6, 0.72, 'Запустить приложение'),
    (7.0, 15.8, 3.6, 0.72, 'Пройти аутентификацию'),
    (7.0, 14.4, 3.6, 0.72, 'Просмотреть главный экран'),
    (7.0, 13.0, 3.6, 0.72, 'Получить список трансляций'),
    (7.0, 11.6, 3.6, 0.72, 'Выбрать трансляцию'),
    (7.0, 10.2, 3.6, 0.72, 'Просмотреть детали матча'),
    (7.0,  8.8, 3.6, 0.72, 'Запустить воспроизведение'),
    (7.0,  7.4, 3.6, 0.72, 'Управлять воспроизведением'),
    (7.0,  6.0, 3.6, 0.72, 'Просмотреть расписание'),
    (7.0,  4.6, 3.8, 0.72, 'Получить уведомление об ошибке'),
]

for (cx, cy, w, h, txt) in uc_viewer:
    usecase(ax, cx, cy, w, h, txt)
    line(ax, 1.3 + 0.45, cy + 0.25, cx - w/2, cy)  # arm from actor midpoint

# ===================== USE CASES — Администратор =====================
uc_admin = [
    (7.5, 16.6, 3.8, 0.72, 'Настроить сервер трансляций'),
    (7.5, 15.2, 3.6, 0.72, 'Управлять каталогом событий'),
]

# redraw — these overlap with viewer; use admin-specific positions at upper right
uc_admin2 = [
    (10.5, 17.0, 3.8, 0.72, 'Настроить сервер\nтрансляций'),
    (10.5, 15.5, 3.8, 0.72, 'Управлять каталогом\nсобытий'),
    (10.5, 14.0, 3.8, 0.72, 'Провести тестовый\nзапуск трансляции'),
]

for (cx, cy, w, h, txt) in uc_admin2:
    usecase(ax, cx, cy, w, h, txt)
    line(ax, 14.7 - 0.45, 15.5 + 0.25, cx + w/2, cy)

# ===================== INCLUDE / EXTEND =====================
# «include»: Запустить воспроизведение includes Загрузить поток
usecase(ax, 5.8, 8.0, 3.4, 0.68, '«include»\nЗагрузить HLS-поток')
dashed_arrow(ax, 7.0, 8.8 - 0.36, 5.8, 8.0 + 0.34, '«include»')

# «extend»: Управлять воспроизведением extends Пауза/Перемотка
usecase(ax, 4.5, 6.8, 3.4, 0.68, '«extend»\nПоставить на паузу')
dashed_arrow(ax, 4.5, 7.4, 4.5, 6.8 + 0.34, '«extend»')

# ===================== LEGEND =====================
ax.text(0.3, 0.7, 'Обозначения:', fontsize=8, fontweight='bold')
actor(ax, 0.7, -0.1, '')
ax.text(1.2, 0.1, '— Актор', fontsize=8)
ell_leg = Ellipse((3.2, 0.2), 1.4, 0.45, fill=True, facecolor='#F5F5F5',
                  edgecolor='black', linewidth=1.2)
ax.add_patch(ell_leg)
ax.text(3.2, 0.2, 'Вариант использования', ha='center', va='center', fontsize=6)
ax.text(4.2, 0.2, '— Вариант использования', fontsize=8)

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/01_use_case.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('Use case diagram saved.')
