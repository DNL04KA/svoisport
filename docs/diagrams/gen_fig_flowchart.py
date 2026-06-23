"""Блок-схема алгоритма просмотра трансляции (ГОСТ-стиль)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import numpy as np

fig, ax = plt.subplots(figsize=(18, 26))
ax.set_xlim(0, 18)
ax.set_ylim(0, 26)
ax.axis('off')
fig.patch.set_facecolor('white')

# ─── helpers ───────────────────────────────────────────────────────────────────

def arw(x1, y1, x2, y2):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color='black', lw=1.1, zorder=5))

def ln(*pts):
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    ax.plot(xs, ys, 'k-', lw=1.1, zorder=2)

def lbl(x, y, t, ha='left', va='center', fs=7.8):
    ax.text(x, y, t, ha=ha, va=va, fontsize=fs, color='black', zorder=6)

def oval(cx, cy, w=3.2, h=0.65, text='', fs=9):
    ax.add_patch(mpatches.FancyBboxPatch(
        (cx - w/2, cy - h/2), w, h, boxstyle='round,pad=0.15',
        facecolor='white', edgecolor='black', linewidth=1.3, zorder=3))
    ax.text(cx, cy, text, ha='center', va='center', fontsize=fs, zorder=4)

def rect(cx, cy, w=4.2, h=0.80, text='', fs=8.5):
    ax.add_patch(mpatches.Rectangle(
        (cx - w/2, cy - h/2), w, h,
        facecolor='white', edgecolor='black', linewidth=1.2, zorder=3))
    ax.text(cx, cy, text, ha='center', va='center', fontsize=fs, zorder=4,
            multialignment='center')

def diamond(cx, cy, w=4.6, h=1.1, text='', fs=8.5):
    pts = np.array([[cx, cy+h/2], [cx+w/2, cy], [cx, cy-h/2], [cx-w/2, cy]])
    ax.add_patch(plt.Polygon(pts, facecolor='white', edgecolor='black',
                              linewidth=1.2, zorder=3))
    ax.text(cx, cy, text, ha='center', va='center', fontsize=fs, zorder=4,
            multialignment='center')

def parallelogram(cx, cy, w=4.2, h=0.80, text='', fs=8.0, sk=0.30):
    pts = np.array([
        [cx - w/2 + sk, cy + h/2], [cx + w/2 + sk, cy + h/2],
        [cx + w/2 - sk, cy - h/2], [cx - w/2 - sk, cy - h/2]
    ])
    ax.add_patch(plt.Polygon(pts, facecolor='white', edgecolor='black',
                              linewidth=1.2, zorder=3))
    ax.text(cx, cy, text, ha='center', va='center', fontsize=fs, zorder=4,
            multialignment='center')

def conn(cx, cy, label='А', fs=9.5):
    ax.add_patch(plt.Circle((cx, cy), 0.32,
                            facecolor='white', edgecolor='black', lw=1.3, zorder=3))
    ax.text(cx, cy, label, ha='center', va='center',
            fontsize=fs, fontweight='bold', zorder=4)

# ─── constants ─────────────────────────────────────────────────────────────────
MX  = 7.0    # main column x
BW  = 4.2    # block width
BH  = 0.80   # block height
DW  = 4.6    # diamond width
DH  = 1.1    # diamond height
RX  = 14.0   # right error column x
RBW = 4.0    # right column block width

# ─── title ─────────────────────────────────────────────────────────────────────
ax.text(9.0, 25.4, 'Блок-схема алгоритма просмотра трансляции',
        ha='center', va='center', fontsize=12, fontweight='bold')

# ─── MAIN FLOW ─────────────────────────────────────────────────────────────────

# Начало  y=24.5
oval(MX, 24.5, text='Начало')
arw(MX, 24.18, MX, 23.68)

# 1. Инициализация компонентов  y=23.3
rect(MX, 23.3, w=BW, h=BH, text='Инициализация компонентов')
arw(MX, 22.90, MX, 22.38)

# 2. Запрос к серверу  y=22.0
rect(MX, 22.0, w=BW, h=BH, text='Запрос списка трансляций\nк серверу')
arw(MX, 21.60, MX, 21.00)

# D1: Ответ получен?  y=20.45
diamond(MX, 20.45, w=DW, h=DH, text='Ответ\nполучен?')
lbl(MX + 0.14, 20.13, 'Да')
arw(MX, 19.90, MX, 19.38)

# 3. Список трансляций  y=19.0
parallelogram(MX, 19.0, w=BW, h=BH, text='Отображение списка трансляций')
arw(MX, 18.60, MX, 18.08)

# 4. Выбор трансляции  y=17.7
parallelogram(MX, 17.7, w=BW, h=BH, text='Пользователь выбирает трансляцию')
arw(MX, 17.30, MX, 16.78)

# 5. Запрос параметров  y=16.4
rect(MX, 16.4, w=BW, h=BH, text='Запрос параметров потока')
arw(MX, 16.00, MX, 15.38)

# D2: Токен получен?  y=14.83
diamond(MX, 14.83, w=DW, h=DH, text='Токен\nполучен?')
lbl(MX + 0.14, 14.50, 'Да')
arw(MX, 14.28, MX, 13.75)

# 6. Инициализация плеера  y=13.37
rect(MX, 13.37, w=BW, h=BH, text='Инициализация видеоплеера')
arw(MX, 12.97, MX, 12.45)

# 7. Начало воспроизведения  y=12.07
rect(MX, 12.07, w=BW, h=BH, text='Начало воспроизведения')
arw(MX, 11.67, MX, 11.15)

# Connector А (incoming) → 8. Ожидание команды  y=10.77
conn(MX - 2.8, 10.77, 'А')
arw(MX - 2.48, 10.77, MX - BW/2, 10.77)
rect(MX, 10.77, w=BW, h=BH, text='Ожидание команды пользователя')
arw(MX, 10.37, MX, 9.78)

# D3: Команда?  y=9.22  (3-way: left=Пауза, right=Продолжить, down=Выход)
CMD_W = 5.2
diamond(MX, 9.22, w=CMD_W, h=DH, text='Команда?')

# Left branch: Пауза → Приостановить → conn А  (Г-образный путь)
LPX = 2.8   # pause block center x
# diamond left tip → horizontal left → vertical down into block top
ln((MX - CMD_W/2, 9.22), (LPX, 9.22))
arw(LPX, 9.22, LPX, 8.35 + 0.375)
lbl((MX - CMD_W/2 + LPX)/2, 9.34, 'Пауза', ha='center')
rect(LPX, 8.35, w=3.4, h=0.75, text='Приостановить\nвоспроизведение', fs=7.5)
ln((LPX, 7.97), (LPX, 7.48))
conn(LPX, 7.15, 'А')

# Right branch: Продолжить → Возобновить → conn А  (Г-образный путь)
RPX = 11.4   # resume block center x
# diamond right tip → horizontal right → vertical down into block top
ln((MX + CMD_W/2, 9.22), (RPX, 9.22))
arw(RPX, 9.22, RPX, 8.35 + 0.375)
lbl((MX + CMD_W/2 + RPX)/2, 9.34, 'Продолжить', ha='center')
rect(RPX, 8.35, w=3.4, h=0.75, text='Возобновить\nвоспроизведение', fs=7.5)
ln((RPX, 7.97), (RPX, 7.48))
conn(RPX, 7.15, 'А')

# Down branch: Выход → D4
lbl(MX + 0.14, 8.90, 'Выход')
arw(MX, 8.67, MX, 8.10)

# D4: Выход подтверждён?  y=7.55
diamond(MX, 7.55, w=4.8, h=DH, text='Выход\nподтверждён?')

# Нет → conn А (to left)
lbl(MX - 4.8/2 - 0.12, 7.65, 'Нет', ha='right')
ln((MX - 2.4, 7.55), (1.3, 7.55))
arw(1.3, 7.55, 1.3, 7.15 + 0.32)
conn(1.3, 6.80, 'А')

# Да → Сохранение
lbl(MX + 0.14, 7.22, 'Да')
arw(MX, 7.00, MX, 6.48)

# Сохранение данных  y=6.10
rect(MX, 6.10, w=BW, h=BH, text='Сохранение данных сеанса в БД')
arw(MX, 5.70, MX, 5.20)

# Конец  y=4.87
oval(MX, 4.87, text='Конец')

# ─── RIGHT: Network error branch ───────────────────────────────────────────────
# From D1 Нет → right
lbl(MX + DW/2 + 0.12, 20.55, 'Нет')
arw(MX + DW/2, 20.45, RX - RBW/2, 20.45)

# Ошибка сети
parallelogram(RX, 20.45, w=RBW, h=BH, text='Вывод сообщения\nоб ошибке сети', fs=7.5)
arw(RX, 20.05, RX, 19.45)

# D5: Повторить запрос?  y=18.9
diamond(RX, 18.9, w=4.2, h=DH, text='Повторить\nзапрос?')

# Да → back to Block 2 (Запрос к серверу at y=22.0)
lbl(RX - 4.2/2 - 0.12, 18.98, 'Да', ha='right')
ln((RX - 2.1, 18.9), (10.5, 18.9), (10.5, 22.0))
arw(10.5, 22.0, MX + BW/2, 22.0)

# Нет → Закрыть → Конец
lbl(RX + 0.14, 18.57, 'Нет')
arw(RX, 18.35, RX, 17.82)
rect(RX, 17.43, w=RBW, h=BH, text='Закрыть приложение')
arw(RX, 17.03, RX, 16.50)
oval(RX, 16.15, w=3.0, h=0.65, text='Конец')

# ─── RIGHT: Token error branch ─────────────────────────────────────────────────
# From D2 Нет → right
lbl(MX + DW/2 + 0.12, 14.93, 'Нет')
arw(MX + DW/2, 14.83, RX - RBW/2, 14.83)

# Ошибка токена
parallelogram(RX, 14.83, w=RBW, h=BH, text='Вывод сообщения\nоб ошибке токена', fs=7.5)
arw(RX, 14.43, RX, 13.90)
rect(RX, 13.50, w=RBW, h=BH, text='Закрыть приложение')
arw(RX, 13.10, RX, 12.57)
oval(RX, 12.22, w=3.0, h=0.65, text='Конец')

# ─── legend ────────────────────────────────────────────────────────────────────
lx, ly = 0.35, 4.20
ax.text(lx, ly, 'Условные обозначения:', fontsize=8, fontweight='bold')

# Oval — начало/конец
oval(lx + 1.0, ly - 0.55, w=1.6, h=0.50, text='', fs=1)
ln((lx + 1.8, ly - 0.55), (lx + 2.1, ly - 0.55))
lbl(lx + 2.2, ly - 0.55, '— начало / конец алгоритма')

# Rect — процесс
ax.add_patch(mpatches.Rectangle((lx + 0.2, ly - 1.2), 1.6, 0.50,
             facecolor='white', edgecolor='black', lw=1.2, zorder=3))
ln((lx + 1.8, ly - 0.95), (lx + 2.1, ly - 0.95))
lbl(lx + 2.2, ly - 0.95, '— процесс (вычисление / действие)')

# Parallelogram — ввод/вывод
pts = np.array([
    [lx + 0.35, ly - 1.38],
    [lx + 1.95, ly - 1.38],
    [lx + 1.75, ly - 1.63],
    [lx + 0.15, ly - 1.63]
])
ax.add_patch(plt.Polygon(pts, facecolor='white', edgecolor='black', lw=1.2, zorder=3))
ln((lx + 1.95, ly - 1.505), (lx + 2.1, ly - 1.505))
lbl(lx + 2.2, ly - 1.505, '— ввод / вывод данных')

# Diamond — решение
dpts = np.array([
    [lx + 1.05, ly - 1.75],
    [lx + 1.80, ly - 1.97],
    [lx + 1.05, ly - 2.19],
    [lx + 0.30, ly - 1.97]
])
ax.add_patch(plt.Polygon(dpts, facecolor='white', edgecolor='black', lw=1.2, zorder=3))
ln((lx + 1.80, ly - 1.97), (lx + 2.1, ly - 1.97))
lbl(lx + 2.2, ly - 1.97, '— условие (ветвление)')

# Connector
conn(lx + 1.05, ly - 2.55, 'А', fs=8)
ln((lx + 1.37, ly - 2.55), (lx + 2.1, ly - 2.55))
lbl(lx + 2.2, ly - 2.55, '— соединитель (переход на ту же страницу)')

plt.savefig(
    '/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/11_flowchart_stream.png',
    dpi=180, bbox_inches='tight', facecolor='white')
print('Saved 11_flowchart_stream.png')
