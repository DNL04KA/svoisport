"""State Diagram — Диаграмма состояний сеанса просмотра (ГОСТ 19.701-90 / UML)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch
import matplotlib.patheffects as pe

fig, ax = plt.subplots(figsize=(15, 20))
ax.set_xlim(0, 15)
ax.set_ylim(0, 20)
ax.axis('off')
fig.patch.set_facecolor('white')

# ---- helpers ----
def state(ax, cx, cy, w, h, name, inner='', fontsize=9.5):
    rect = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                          boxstyle='round,pad=0.12',
                          facecolor='#E8EEF4', edgecolor='#1A3A5C', linewidth=2, zorder=3)
    ax.add_patch(rect)
    if inner:
        # header line
        ax.plot([cx - w/2, cx + w/2], [cy + h/2 - 0.45, cy + h/2 - 0.45],
                color='#1A3A5C', lw=1.2, zorder=4)
        ax.text(cx, cy + h/2 - 0.22, name, ha='center', va='center',
                fontsize=fontsize, fontweight='bold', zorder=5)
        ax.text(cx, cy - 0.08, inner, ha='center', va='center',
                fontsize=7.5, zorder=5, style='italic', color='#333333')
    else:
        ax.text(cx, cy, name, ha='center', va='center',
                fontsize=fontsize, fontweight='bold', zorder=5)

def init_point(ax, cx, cy, r=0.22):
    ax.add_patch(plt.Circle((cx, cy), r, facecolor='black', edgecolor='black', zorder=4))

def final_point(ax, cx, cy, r=0.22):
    ax.add_patch(plt.Circle((cx, cy), r + 0.12, facecolor='white', edgecolor='black',
                             linewidth=2, zorder=4))
    ax.add_patch(plt.Circle((cx, cy), r, facecolor='black', edgecolor='black', zorder=5))

def trans(ax, x1, y1, x2, y2, label='', lw=1.4, color='#1A3A5C',
          rad=0.0, label_dx=0.0, label_dy=0.1, fontsize=8):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                connectionstyle=f'arc3,rad={rad}'))
    if label:
        mx = (x1 + x2) / 2 + label_dx
        my = (y1 + y2) / 2 + label_dy
        ax.text(mx, my, label, ha='center', va='center', fontsize=fontsize,
                color='#222222',
                bbox=dict(boxstyle='round,pad=0.15', facecolor='white',
                          edgecolor='none', alpha=0.85))

# ===================== TITLE =====================
ax.text(7.5, 19.6, 'Диаграмма состояний сеанса просмотра трансляции',
        ha='center', va='top', fontsize=13, fontweight='bold')
ax.text(7.5, 19.1, '(Приложение «СвойСпорт TV»)',
        ha='center', va='top', fontsize=9.5, color='#444444')

# ===================== STATES =====================
# Инициализация
state(ax, 7.5, 17.2, 4.0, 0.85, 'Инициализация',
      'entry / запуск PlayerManager')

# Загрузка параметров потока
state(ax, 7.5, 15.3, 4.2, 0.85, 'Загрузка параметров',
      'do / HTTP-запрос URL потока')

# Буферизация
state(ax, 7.5, 13.4, 4.2, 0.85, 'Буферизация',
      'do / ExoPlayer.prepare()')

# Воспроизведение
state(ax, 4.5, 11.0, 4.2, 0.85, 'Воспроизведение',
      'do / ExoPlayer.play()')

# Пауза
state(ax, 4.5, 8.8, 3.8, 0.85, 'Пауза',
      'do / ExoPlayer.pause()')

# Ошибка
state(ax, 10.5, 11.0, 4.0, 0.85, 'Ошибка',
      'entry / показать сообщение об ошибке')

# Повтор подключения
state(ax, 10.5, 8.8, 4.0, 0.85, 'Повтор подключения',
      'do / retry (max 3 попытки)')

# Завершён
state(ax, 7.5, 6.3, 3.8, 0.85, 'Завершён',
      'exit / PlayerManager.release()')

# ===================== INITIAL / FINAL =====================
init_point(ax, 7.5, 18.6)
final_point(ax, 7.5, 4.8)

# ===================== TRANSITIONS =====================
# Старт → Инициализация
trans(ax, 7.5, 18.37, 7.5, 17.62, 'Запуск плеера')

# Инициализация → Загрузка параметров
trans(ax, 7.5, 16.77, 7.5, 15.73, 'matchId получен')

# Загрузка → Буферизация
trans(ax, 7.5, 14.87, 7.5, 13.83, 'URL потока получен')

# Буферизация → Воспроизведение
trans(ax, 5.9, 12.97, 4.9, 11.43, 'Буфер заполнен')

# Воспроизведение → Пауза
trans(ax, 4.5, 10.57, 4.5, 9.22,
      'Команда «Пауза»', rad=0.3, label_dx=-1.5)

# Пауза → Воспроизведение
trans(ax, 4.5, 9.22, 4.5, 10.57,
      'Команда «Продолжить»', rad=0.3, label_dx=1.55)

# Воспроизведение → Ошибка
trans(ax, 6.6, 11.0, 8.5, 11.0,
      'Потеря соединения / ошибка потока')

# Буферизация → Ошибка
trans(ax, 9.6, 13.4, 10.5, 11.43,
      'Таймаут / сервер недоступен', label_dx=1.4, label_dy=0.0)

# Ошибка → Повтор подключения
trans(ax, 10.5, 10.57, 10.5, 9.22,
      'Повторная попытка', rad=0.3, label_dx=1.7)

# Повтор → Буферизация (успех)
trans(ax, 9.3, 9.2, 7.5, 13.0,
      'Соединение восстановлено', rad=-0.3, label_dx=-1.8, label_dy=0.2)

# Повтор → Завершён (лимит попыток)
trans(ax, 10.5, 8.37, 9.4, 6.72,
      'Превышен лимит попыток', label_dx=1.6, label_dy=0.0)

# Ошибка → Завершён (отмена)
trans(ax, 10.0, 10.57, 9.4, 6.72,
      'Отмена', rad=0.15, label_dx=1.1, label_dy=0.05)

# Пауза → Завершён
trans(ax, 4.5, 8.37, 5.6, 6.72, 'Команда «Выход»')

# Воспроизведение → Завершён
trans(ax, 5.1, 10.57, 5.8, 6.72,
      'Команда «Выход»', rad=-0.2, label_dx=-1.5, label_dy=0.0)

# Завершён → final
trans(ax, 7.5, 5.87, 7.5, 5.14, '')

# ===================== INTERNAL STATES note =====================
ax.annotate('', xy=(3.0, 10.0), xytext=(2.3, 9.0),
            arrowprops=dict(arrowstyle='->', color='#666666', lw=1.0,
                            linestyle='dotted'))
note = ('Внутренние действия:\n'
        'do / обновление прогресс-бара\n'
        'do / контроль буфера\n'
        'do / heartbeat к серверу')
ax.text(0.3, 8.7, note, fontsize=7.5, color='#333333',
        bbox=dict(boxstyle='round,pad=0.3', facecolor='#FFFFF0',
                  edgecolor='#AAAAAA', alpha=0.9))

# ===================== LEGEND =====================
leg_x, leg_y = 11.5, 4.5
ax.text(leg_x, leg_y, 'Обозначения:', fontsize=8, fontweight='bold')
init_point(ax, leg_x + 0.25, leg_y - 0.5, 0.15)
ax.text(leg_x + 0.6, leg_y - 0.5, '— начальное псевдосостояние', fontsize=7.5, va='center')
final_point(ax, leg_x + 0.25, leg_y - 1.0, 0.13)
ax.text(leg_x + 0.6, leg_y - 1.0, '— конечное состояние', fontsize=7.5, va='center')
samp = FancyBboxPatch((leg_x, leg_y - 1.65), 0.5, 0.38,
                      boxstyle='round,pad=0.05',
                      facecolor='#E8EEF4', edgecolor='#1A3A5C', linewidth=1.5)
ax.add_patch(samp)
ax.text(leg_x + 0.25, leg_y - 1.47, 'S', ha='center', va='center', fontsize=7.5, fontweight='bold')
ax.text(leg_x + 0.6, leg_y - 1.47, '— состояние', fontsize=7.5, va='center')

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/02_state_diagram.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('State diagram saved.')
