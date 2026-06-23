"""DFD — Диаграмма потоков данных процесса «Просмотр трансляции» (нотация Йордона-ДеМарко)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Ellipse, FancyArrowPatch
import matplotlib.patheffects as pe
import numpy as np

fig, ax = plt.subplots(figsize=(20, 15))
ax.set_xlim(0, 20)
ax.set_ylim(0, 15)
ax.axis('off')
fig.patch.set_facecolor('white')

# ---- helpers ----
def process(ax, cx, cy, r, num, title, fontsize=8.5):
    """Circle = process (Yourdon)"""
    circ = plt.Circle((cx, cy), r, facecolor='#D6E4F0', edgecolor='#1A3A5C',
                      linewidth=2.0, zorder=3)
    ax.add_patch(circ)
    # dividing arc (number top)
    theta = np.linspace(np.pi*0.2, np.pi*0.8, 60)
    ax.plot(cx + r*np.cos(theta), cy + r*np.sin(theta),
            color='#1A3A5C', lw=1.5, zorder=4)
    ax.text(cx, cy + r*0.6, num, ha='center', va='center',
            fontsize=7.5, fontweight='bold', color='#1A3A5C', zorder=5)
    lines = title.split('\n')
    for i, line in enumerate(lines):
        offset = (len(lines) - 1) / 2 * 0.22 - i * 0.26
        ax.text(cx, cy - 0.05 + offset, line, ha='center', va='center',
                fontsize=fontsize, zorder=5)

def external(ax, cx, cy, w, h, name, fontsize=9):
    """Rectangle = external entity"""
    r = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                       boxstyle='square,pad=0',
                       facecolor='#F0F0F0', edgecolor='#333333', linewidth=2.0, zorder=3)
    ax.add_patch(r)
    lines = name.split('\n')
    for i, line in enumerate(lines):
        offset = (len(lines) - 1) / 2 * 0.22 - i * 0.28
        ax.text(cx, cy + offset, line, ha='center', va='center',
                fontsize=fontsize, fontweight='bold', zorder=4)

def datastore(ax, cx, cy, w, h, num, name, fontsize=8.5):
    """Open rectangle (2 horizontal lines) = data store"""
    # top line
    ax.plot([cx - w/2, cx + w/2], [cy + h/2, cy + h/2], color='#1A3A5C', lw=2.0, zorder=3)
    # bottom line
    ax.plot([cx - w/2, cx + w/2], [cy - h/2, cy - h/2], color='#1A3A5C', lw=2.0, zorder=3)
    # left cap
    ax.plot([cx - w/2, cx - w/2], [cy - h/2, cy + h/2], color='#1A3A5C', lw=2.0, zorder=3)
    # fill
    r = FancyBboxPatch((cx - w/2 + 0.02, cy - h/2 + 0.02), w - 0.04, h - 0.04,
                       boxstyle='square,pad=0',
                       facecolor='#FFF9E0', edgecolor='none', linewidth=0, zorder=2)
    ax.add_patch(r)
    ax.text(cx - w/2 + 0.25, cy, num, ha='left', va='center',
            fontsize=8, fontweight='bold', color='#555555', zorder=4)
    ax.plot([cx - w/2 + 0.7, cx - w/2 + 0.7], [cy - h/2, cy + h/2],
            color='#AAAAAA', lw=1.0, zorder=3, ls='--')
    ax.text(cx - w/2 + 0.85, cy, name, ha='left', va='center',
            fontsize=fontsize, zorder=4)

def flow(ax, x1, y1, x2, y2, label='', rad=0.0, lbl_dx=0.0, lbl_dy=0.15,
         color='#1A3A5C', fontsize=7.8, lw=1.5):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw, mutation_scale=14,
                                connectionstyle=f'arc3,rad={rad}'))
    if label:
        mx = (x1 + x2) / 2 + lbl_dx
        my = (y1 + y2) / 2 + lbl_dy
        ax.text(mx, my, label, ha='center', va='center', fontsize=fontsize,
                color='#222222',
                bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                          edgecolor='none', alpha=0.88), zorder=6)

def seg(ax, x1, y1, x2, y2, color='#1A3A5C', lw=1.5):
    ax.plot([x1, x2], [y1, y2], color=color, lw=lw, zorder=4)

def dot(ax, x, y, color='#1A3A5C'):
    ax.plot(x, y, 'o', color=color, ms=5, zorder=6)

# ===================== TITLE =====================
ax.text(10.0, 14.7, 'DFD — Диаграмма потоков данных',
        ha='center', va='top', fontsize=13, fontweight='bold')
ax.text(10.0, 14.2, 'Процесс: «Просмотр трансляции»  |  Нотация: Yourdon-DeMarco  |  Дата: 2026-05-24',
        ha='center', va='top', fontsize=8.5, color='#555555')

# ===================== EXTERNAL ENTITIES =====================
external(ax,  1.4, 11.5, 2.4, 1.0, 'Зритель\n(Пользователь)')
external(ax, 18.4, 11.5, 2.4, 1.0, 'Smart TV\nустройство')
external(ax, 18.4,  3.5, 2.4, 1.0, 'Сервер\nтрансляций')

# ===================== PROCESSES =====================
# P1 — Загрузка каталога
process(ax, 5.5, 11.5, 1.35, 'P1', 'Загрузка\nкаталога')
# P2 — Формирование UI каталога
process(ax, 5.5,  7.5, 1.35, 'P2', 'Формирование\nUI каталога')
# P3 — Инициализация сеанса просмотра
process(ax, 10.0, 11.5, 1.35, 'P3', 'Инициализация\nсеанса')
# P4 — Управление воспроизведением
process(ax, 10.0,  7.0, 1.45, 'P4', 'Управление\nвоспроизведением')
# P5 — Обработка ошибок
process(ax, 15.0,  7.0, 1.35, 'P5', 'Обработка\nошибок')
# P6 — Регистрация сеанса
process(ax, 15.0, 11.5, 1.35, 'P6', 'Регистрация\nсеанса')

# ===================== DATA STORES =====================
datastore(ax, 10.0, 13.3, 5.2, 0.7, 'D1', 'Каталог трансляций')
datastore(ax,  5.5,  4.5, 5.2, 0.7, 'D2', 'Кэш каталога')
datastore(ax, 14.0,  4.5, 5.2, 0.7, 'D3', 'История сеансов')

# ===================== DATA FLOWS =====================

# --- Зритель → P1 ---
flow(ax, 2.62, 11.5, 4.15, 11.5, 'Запрос каталога')

# --- P1 → D1 (запрос к серверу) ---
flow(ax, 5.5, 12.85, 8.4, 13.3, 'Запрос данных\nтрансляций',
     rad=-0.2, lbl_dx=0.3, lbl_dy=0.2)

# --- D1 → P1 (данные) ---
flow(ax, 8.4, 13.1, 5.5, 12.85, 'Данные о трансляциях',
     rad=0.15, lbl_dx=-0.8, lbl_dy=-0.2)

# --- P1 → Сервер трансляций ---
flow(ax, 6.85, 11.5, 17.2, 11.5, 'HTTP-запрос\n(SportTV API)',
     rad=-0.1, lbl_dy=0.2)

# --- Сервер → P1 ---
flow(ax, 17.2, 11.2, 6.85, 11.2, 'JSON список матчей',
     rad=-0.08, lbl_dy=-0.25)

# --- P1 → P2 ---
flow(ax, 5.5, 10.15, 5.5, 8.85, 'Данные каталога')

# --- P1 → D2 (кэш) ---
flow(ax, 5.0, 10.15, 5.0, 5.15, 'Сохранение в кэш',
     rad=0.1, lbl_dx=-1.2)

# --- D2 → P2 ---
flow(ax, 5.5, 5.15, 5.5, 6.15, 'Данные из кэша')

# --- Зритель → P2 (команды навигации) ---
flow(ax, 2.62, 11.0, 4.15, 8.0,
     'Команды\nнавигации', rad=0.15, lbl_dx=-0.6, lbl_dy=0.0)

# --- P2 → Smart TV (экран каталога) ---
flow(ax, 6.85, 7.5, 17.2, 11.0, 'Экран каталога\n(Compose UI)',
     rad=0.1, lbl_dx=0.5, lbl_dy=0.2)

# --- Зритель → P3 (выбор трансляции) ---
flow(ax, 2.62, 12.0, 8.65, 11.8, 'Выбор трансляции\n(matchId)',
     rad=-0.15, lbl_dy=0.2)

# --- P3 → Сервер (запрос URL потока) ---
flow(ax, 11.35, 11.5, 17.2, 11.8, 'Запрос URL\nпотока (matchId)',
     rad=-0.1, lbl_dy=0.2)

# --- Сервер → P3 ---
flow(ax, 17.2, 11.2, 11.35, 11.2, 'URL потока\n(HLS/DASH)',
     rad=-0.08, lbl_dy=-0.25)

# --- P3 → P4 ---
flow(ax, 10.0, 10.15, 10.0, 8.45, 'Параметры потока\n(URL, title, isLive)')

# --- P3 → P6 (регистрация) ---
flow(ax, 11.35, 11.5, 13.65, 11.5, 'Данные сеанса\n(matchId, userId, time)')

# --- P6 → D3 ---
flow(ax, 15.0, 10.15, 15.0, 5.15, 'Запись сеанса')

# --- D3 → P6 (обратная связь) ---
flow(ax, 14.5, 5.15, 14.5, 10.15, 'Подтверждение\nзаписи', lbl_dx=-1.0)

# --- Зритель → P4 (команды плеера) ---
flow(ax, 2.62, 11.0, 8.55, 7.5,
     'Команды плеера\n(пауза / выход / перемотка)',
     rad=0.2, lbl_dx=-0.3, lbl_dy=0.0)

# --- P4 → Smart TV (видеопоток) ---
flow(ax, 11.45, 7.0, 17.2, 11.0, 'Видеопоток\nна экране',
     rad=0.2, lbl_dx=1.0, lbl_dy=-0.1)

# --- P4 → P5 (ошибки) ---
flow(ax, 11.45, 7.0, 13.65, 7.0, 'Событие ошибки\n(код, тип)')

# --- P5 → P4 (повтор) ---
flow(ax, 13.65, 6.6, 11.45, 6.6, 'Команда повтора',
     rad=-0.15, lbl_dy=-0.25)

# --- P5 → Smart TV (уведомление) ---
flow(ax, 16.35, 7.0, 17.2, 11.0, 'Уведомление\nоб ошибке',
     rad=0.1, lbl_dx=0.6, lbl_dy=0.0)

# ===================== LEGEND =====================
lx, ly = 0.3, 4.2
ax.text(lx, ly, 'Обозначения:', fontsize=8.5, fontweight='bold')

# process symbol
circ2 = plt.Circle((lx + 0.45, ly - 0.6), 0.35, facecolor='#D6E4F0',
                   edgecolor='#1A3A5C', linewidth=1.5, zorder=3)
ax.add_patch(circ2)
ax.text(lx + 0.45, ly - 0.6, 'P', ha='center', va='center', fontsize=7)
ax.text(lx + 0.95, ly - 0.6, '— Процесс', fontsize=7.5, va='center')

# external entity
rect2 = FancyBboxPatch((lx + 0.1, ly - 1.35), 0.7, 0.45,
                       boxstyle='square,pad=0',
                       facecolor='#F0F0F0', edgecolor='#333333', linewidth=1.5)
ax.add_patch(rect2)
ax.text(lx + 0.45, ly - 1.12, 'E', ha='center', va='center', fontsize=7)
ax.text(lx + 0.95, ly - 1.12, '— Внешняя сущность', fontsize=7.5, va='center')

# data store
ax.plot([lx + 0.1, lx + 0.8], [ly - 1.65, ly - 1.65], color='#1A3A5C', lw=1.5)
ax.plot([lx + 0.1, lx + 0.8], [ly - 2.0, ly - 2.0], color='#1A3A5C', lw=1.5)
ax.plot([lx + 0.1, lx + 0.1], [ly - 2.0, ly - 1.65], color='#1A3A5C', lw=1.5)
ax.text(lx + 0.95, ly - 1.82, '— Хранилище данных', fontsize=7.5, va='center')

# flow arrow
ax.annotate('', xy=(lx + 0.8, ly - 2.4), xytext=(lx + 0.1, ly - 2.4),
            arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5, mutation_scale=12))
ax.text(lx + 0.95, ly - 2.4, '— Поток данных', fontsize=7.5, va='center')

# ===================== FRAME =====================
outer = FancyBboxPatch((0.1, 0.2), 19.8, 14.4, boxstyle='square,pad=0',
                       facecolor='none', edgecolor='#333333', linewidth=2.0, zorder=0)
ax.add_patch(outer)

plt.tight_layout(pad=0.3)
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/05_dfd.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('DFD saved.')
