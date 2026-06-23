import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

fig, ax = plt.subplots(figsize=(13, 26))
ax.set_xlim(0, 13)
ax.set_ylim(0, 26)
ax.axis('off')
fig.patch.set_facecolor('white')

W = 3.0
H = 0.75
cx = 5.5  # центр основной колонки

def rect(x, y, w=W, h=H, text='', fs=8):
    ax.add_patch(plt.Rectangle((x-w/2, y-h/2), w, h,
        lw=1.3, ec='black', fc='white', zorder=3))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs,
            multialignment='center', zorder=4)

def para(x, y, w=W, h=H, text='', fs=8):
    off = 0.22
    ax.add_patch(plt.Polygon([
        [x-w/2+off, y+h/2], [x+w/2+off, y+h/2],
        [x+w/2-off, y-h/2], [x-w/2-off, y-h/2]
    ], lw=1.3, ec='black', fc='white', zorder=3))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs,
            multialignment='center', zorder=4)

def oval(x, y, w=2.0, h=0.6, text='', fs=9):
    ax.add_patch(mpatches.Ellipse((x, y), w, h,
        lw=1.3, ec='black', fc='white', zorder=3))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs,
            fontweight='bold', zorder=4)

def diamond(x, y, w=3.2, h=1.0, text='', fs=8):
    ax.add_patch(plt.Polygon([
        [x, y+h/2], [x+w/2, y], [x, y-h/2], [x-w/2, y]
    ], lw=1.3, ec='black', fc='white', zorder=3))
    ax.text(x, y, text, ha='center', va='center', fontsize=fs,
            multialignment='center', zorder=4)

def arr(x1, y1, x2, y2):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
        arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)

def lbl(x, y, text, fs=8):
    ax.text(x, y, text, ha='center', va='center', fontsize=fs, zorder=6,
            backgroundcolor='white')

# ========= ОСНОВНАЯ КОЛОНКА =========
oval(cx, 25,    text='Начало')
rect(cx, 23.8,  text='Получить адрес API и ключ\nдоступа из локальной БД')
diamond(cx, 22.5, text='Есть сетевое\nподключение?')
rect(cx, 21.2,  text='Отправить HTTP-запрос\nк API-серверу')
diamond(cx, 19.9, text='Сервер вернул\nданные?')
rect(cx, 18.6,  text='Разобрать данные и\nпреобразовать в объекты модели')
rect(cx, 17.4,  text='Сохранить список событий\nво временное хранилище')
para(cx, 16.2,  text='Отобразить каталог\nтрансляций на экране')
diamond(cx, 14.9, text='Пользователь выбрал\nсобытие?')
rect(cx, 13.6,  text='Перейти к алгоритму\nпросмотра трансляции')
oval(cx, 12.4,  text='Конец')

# ========= ПРАВАЯ КОЛОНКА =========
rx = 10.0
rect(rx, 22.5, w=3.0, text='Вывести уведомление\nоб отсутствии сети')
diamond(rx, 21.2, w=3.0, h=0.9, text='Повторить\nпопытку?')
oval(rx, 20.1, w=1.8, h=0.55, text='Конец')

# ========= ЛЕВАЯ КОЛОНКА =========
lx = 1.5
rect(lx, 19.9, w=2.6, text='Вывести сообщение\nоб ошибке')

# ========= СТРЕЛКИ ОСНОВНОЙ КОЛОНКИ =========
arr(cx, 24.7,  cx, 24.175)
arr(cx, 23.425, cx, 23.0)
arr(cx, 22.0,  cx, 21.575)
lbl(cx+0.25, 21.75, 'Да')
arr(cx, 20.825, cx, 20.4)
arr(cx, 19.4,  cx, 18.975)
lbl(cx+0.25, 19.15, 'Да')
arr(cx, 18.225, cx, 17.775)
arr(cx, 17.025, cx, 16.575)
arr(cx, 15.825, cx, 15.4)
arr(cx, 14.4,  cx, 13.975)
lbl(cx+0.25, 14.15, 'Да')
arr(cx, 13.225, cx, 12.7)

# ========= СТРЕЛКИ ПРАВОЙ ВЕТКИ =========

# Ромб сеть → уведомление (Нет, вправо)
ax.annotate('', xy=(rx-1.5, 22.5), xytext=(cx+1.6, 22.5),
    arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)
lbl(cx+2.2, 22.65, 'Нет')

# Уведомление → ромб повторить
arr(rx, 22.125, rx, 21.65)

# Ромб повторить → Конец (Нет, вниз)
arr(rx, 20.75, rx, 20.375)
lbl(rx+0.35, 20.55, 'Нет')

# Ромб повторить Да → вверх по правому краю → обратно к ромбу сеть
ax.plot([rx+1.5, rx+1.5], [21.2, 22.5], 'k-', lw=1.3, zorder=5)
ax.plot([rx+1.5, rx+1.5, cx+1.6], [22.5, 22.5, 22.5], 'k-', lw=1.3, zorder=5)
# стрелка от правого края обратно к уведомлению
ax.annotate('', xy=(rx+1.5, 21.2), xytext=(rx+1.0, 21.2),
    arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)
ax.plot([rx+1.5, rx+1.0], [21.2, 21.2], 'k-', lw=1.3, zorder=5)
lbl(rx+1.9, 21.2, 'Да')

# ========= СТРЕЛКИ ЛЕВОЙ ВЕТКИ =========

# Ромб данные → ошибка (Нет, влево)
ax.annotate('', xy=(lx+1.3, 19.9), xytext=(cx-1.6, 19.9),
    arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)
lbl(cx-2.1, 20.05, 'Нет')

# Ошибка → повторить запрос (по левому краю вверх)
ax.plot([lx-1.3, lx-1.3], [19.9, 21.2], 'k-', lw=1.3, zorder=5)
ax.plot([lx-1.3, cx-1.5], [21.2, 21.2], 'k-', lw=1.3, zorder=5)
ax.annotate('', xy=(cx-1.5, 21.2), xytext=(cx-1.6, 21.2),
    arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)

# ========= СТРЕЛКА НЕТ (выбор события) =========
ax.plot([cx-1.6, cx-3.0, cx-3.0], [14.9, 14.9, 12.4], 'k-', lw=1.3, zorder=5)
ax.annotate('', xy=(cx-1.0, 12.4), xytext=(cx-3.0, 12.4),
    arrowprops=dict(arrowstyle='->', color='black', lw=1.3), zorder=5)
lbl(cx-3.3, 14.9, 'Нет')

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/flowchart_schedule.png',
            dpi=150, bbox_inches='tight', facecolor='white')
print("Done!")
