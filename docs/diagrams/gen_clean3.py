#!/usr/bin/env python3
"""
Чистые диаграммы без пересечений линий — «СвойСпорт TV»
Стиль: как на референсе (простые прямоугольники, тень, минимализм)
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, FancyBboxPatch
import numpy as np, os

OUT = os.path.dirname(os.path.abspath(__file__))

# ──────────────────────────────────────────────────────────────────────────────
#  Базовые примитивы
# ──────────────────────────────────────────────────────────────────────────────
SHD = 0.12   # размер тени

def new_fig(W=20, H=13):
    fig, ax = plt.subplots(figsize=(W, H))
    ax.set_xlim(0, W); ax.set_ylim(0, H); ax.axis('off')
    fig.patch.set_facecolor('white')
    # тонкая внешняя рамка
    ax.add_patch(Rectangle((0.1, 0.1), W-0.2, H-0.2,
                            fc='none', ec='black', lw=1.2, zorder=0))
    return fig, ax

def save(fig, name):
    plt.savefig(f'{OUT}/{name}', dpi=180, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'✓  {name}')

# IDEF0 / DFD прямоугольный блок с тенью
def rect_box(ax, x, y, w, h, lines, num=None, ref='$0',
             fc='white', ec='black', lw=1.4, fs=10, zb=3):
    # тень
    ax.add_patch(Rectangle((x+SHD, y-SHD), w, h,
                            fc='#BBBBBB', ec='none', zorder=zb-1))
    # основной прямоугольник
    ax.add_patch(Rectangle((x, y), w, h,
                            fc=fc, ec=ec, lw=lw, zorder=zb))
    if ref is not None:
        ax.text(x+0.12, y+0.18, ref, fontsize=7.5, va='bottom',
                color='#555', zorder=zb+1)
    if num is not None:
        ax.text(x+w-0.12, y+0.18, str(num), fontsize=8, va='bottom',
                ha='right', fontweight='bold', zorder=zb+1)
    n = len(lines)
    for i, ln in enumerate(lines):
        oy = (n-1)/2*0.30 - i*0.30
        ax.text(x+w/2, y+h/2+oy, ln,
                ha='center', va='center', fontsize=fs, zorder=zb+1)

# Стрелка с подписью
def arrow(ax, x1, y1, x2, y2, lbl='', lx=0, ly=0.18, fs=8.8,
          color='black', lw=1.3):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                mutation_scale=14))
    if lbl:
        mx, my = (x1+x2)/2 + lx, (y1+y2)/2 + ly
        ax.text(mx, my, lbl, ha='center', va='center', fontsize=fs,
                bbox=dict(fc='white', ec='none', pad=0.1), zorder=6)

def seg(ax, x1, y1, x2, y2, lw=1.3, color='black'):
    ax.plot([x1,x2],[y1,y2], color=color, lw=lw, zorder=4)

def dot(ax, x, y):
    ax.plot(x, y, 'o', color='black', ms=4, zorder=6)

def txt(ax, x, y, s, ha='center', va='center', fs=9.5, bold=False):
    fw = 'bold' if bold else 'normal'
    ax.text(x, y, s, ha=ha, va=va, fontsize=fs, fontweight=fw, zorder=5,
            bbox=dict(fc='white', ec='none', pad=0.1))


# ══════════════════════════════════════════════════════════════════════════════
#  1. IDEF0 Контекстная диаграмма (A-0)
# ══════════════════════════════════════════════════════════════════════════════
def gen_idef0_ctx():
    W, H = 20, 13
    fig, ax = new_fig(W, H)

    ax.text(W/2, H-0.35, 'Контекстная диаграмма IDEF0 (A-0)  —  «СвойСпорт TV»',
            ha='center', va='top', fontsize=12, fontweight='bold')

    # ── Центральный блок ──────────────────────────────────────
    BX, BY, BW, BH = 7.0, 4.2, 6.0, 3.6
    rect_box(ax, BX, BY, BW, BH,
             ['Система просмотра', 'спортивных трансляций', 'на Smart TV'],
             num=0, ref='$0', fs=11, lw=1.8)

    LW = 1.4

    # ── ВХОД (слева) ──────────────────────────────────────────
    seg(ax, 0.6, 6.0, BX, 6.0, lw=LW)
    arrow(ax, BX-0.01, 6.0, BX+0.01, 6.0)
    ax.annotate('', xy=(BX, 6.0), xytext=(0.6, 6.0),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, 3.3, 6.25, 'Запрос пользователя\n(навигация, выбор трансляции)', fs=9.5)

    # ── УПРАВЛЕНИЕ (сверху) — одна стрелка ────────────────────
    seg(ax, 10.0, BY+BH, 10.0, 11.0, lw=LW)
    ax.annotate('', xy=(10.0, BY+BH), xytext=(10.0, 11.0),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, 10.0, 11.5,
        'Стандарты качества видео\n(битрейт, разрешение, протоколы HLS/DASH)', fs=9.5)

    # ── ВЫХОДЫ (справа) ───────────────────────────────────────
    # O1 — успех
    seg(ax, BX+BW, 6.8, 19.0, 6.8, lw=LW)
    ax.annotate('', xy=(19.0, 6.8), xytext=(BX+BW, 6.8),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, 16.5, 7.05, 'Успешный просмотр\nтрансляции', fs=9.5)

    # O2 — ошибка
    seg(ax, BX+BW, 5.2, 19.0, 5.2, lw=LW)
    ax.annotate('', xy=(19.0, 5.2), xytext=(BX+BW, 5.2),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, 16.5, 4.95, 'Ошибка воспроизведения\n/ уведомление', fs=9.5)

    # ── МЕХАНИЗМ (снизу) ──────────────────────────────────────
    seg(ax, 10.0, BY, 10.0, 2.0, lw=LW, color='#444')
    ax.annotate('', xy=(10.0, BY), xytext=(10.0, 2.0),
                arrowprops=dict(arrowstyle='->', color='#444', lw=LW, mutation_scale=14))
    txt(ax, 10.0, 1.55,
        'Информационная система\n(SportTV API  +  Android TV OS  +  ExoPlayer)', fs=9.5)

    save(fig, 'clean_01_idef0_ctx.png')


# ══════════════════════════════════════════════════════════════════════════════
#  2. IDEF0 Декомпозиция (A0)  — каскадная компоновка, линии не пересекаются
# ══════════════════════════════════════════════════════════════════════════════
def gen_idef0_decomp():
    W, H = 20, 13
    fig, ax = new_fig(W, H)

    ax.text(W/2, H-0.35, 'Декомпозиция IDEF0 (A0)  —  «СвойСпорт TV»',
            ha='center', va='top', fontsize=12, fontweight='bold')

    LW = 1.3
    BW, BH = 3.8, 2.0   # размер каждого блока

    # ── Блоки (каскад: каждый правее и ниже предыдущего) ──────
    #
    #     A1               Control
    #          A2
    #               A3
    #                    A4
    #
    #     механизм
    #
    boxes = [
        (1.2,  9.0, 'A1', ['Аутентификация', 'пользователя']),
        (5.6,  7.2, 'A2', ['Загрузка каталога', 'трансляций']),
        (10.0, 5.4, 'A3', ['Воспроизведение', 'трансляции']),
        (14.5, 3.6, 'A4', ['Регистрация', 'сеанса просмотра']),
    ]
    for x, y, num, lines in boxes:
        rect_box(ax, x, y, BW, BH, lines, num=num, ref='$0', fs=10, lw=1.6)

    # ── Управление (сверху) — горизонтальная линия + вертикальные ответвления
    ctrl_y = 11.5
    ctrl_x_left  = boxes[0][0] + BW/2
    ctrl_x_right = boxes[-1][0] + BW/2
    seg(ax, ctrl_x_left, ctrl_y, ctrl_x_right, ctrl_y, lw=LW)
    for x, y, _, _ in boxes:
        cx = x + BW/2
        dot(ax, cx, ctrl_y)
        seg(ax, cx, ctrl_y, cx, y+BH, lw=LW)
        ax.annotate('', xy=(cx, y+BH), xytext=(cx, ctrl_y),
                    arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=13))
    txt(ax, (ctrl_x_left+ctrl_x_right)/2, ctrl_y+0.40,
        'Стандарты качества видео / Правила доступа к контенту', fs=9.5)

    # ── Механизм (снизу) — горизонтальная линия + вертикальные
    mech_y = 2.0
    mech_xs = [boxes[0][0]+BW/2, boxes[1][0]+BW/2, boxes[2][0]+BW/2, boxes[3][0]+BW/2]
    seg(ax, mech_xs[0], mech_y, mech_xs[-1], mech_y, lw=LW, color='#444')
    for i, (x, y, _, _) in enumerate(boxes):
        cx = x + BW/2
        dot(ax, cx, mech_y)
        seg(ax, cx, mech_y, cx, y, lw=LW, color='#444')
        ax.annotate('', xy=(cx, y), xytext=(cx, mech_y),
                    arrowprops=dict(arrowstyle='->', color='#444', lw=LW, mutation_scale=13))
    txt(ax, (mech_xs[0]+mech_xs[-1])/2, mech_y-0.45,
        'Информационная система  (SportTV API  +  Android TV OS  +  ExoPlayer)', fs=9.5)

    # ── Вход (слева → A1) ─────────────────────────────────────
    x0, y0, _, _ = boxes[0]
    seg(ax, 0.3, y0+BH/2, x0, y0+BH/2, lw=LW)
    ax.annotate('', xy=(x0, y0+BH/2), xytext=(0.3, y0+BH/2),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=13))
    txt(ax, 0.3, y0+BH/2+0.30, 'Запрос\nпользователя', ha='center', fs=9)

    # ── Потоки между блоками (A1→A2, A2→A3, A3→A4) ───────────
    # Каждый поток: правый бок блока → L-изгиб → левый бок следующего
    flows = [
        (boxes[0], boxes[1], 'Данные аутентификации\n(токен доступа)'),
        (boxes[1], boxes[2], 'URL потока\n(HLS/DASH)'),
        (boxes[2], boxes[3], 'Данные сеанса\n(matchId, userId, время)'),
    ]
    for (x1,y1,_,_), (x2,y2,_,_), lbl in flows:
        # точка выхода — правый бок блока 1, посередине
        px1, py1 = x1+BW, y1+BH/2
        # точка входа  — левый бок блока 2, посередине
        px2, py2 = x2,    y2+BH/2
        # горизонталь от блока 1, потом вертикаль вниз, потом горизонталь к блоку 2
        mid_x = px1 + 0.4
        dot(ax, mid_x, py1)
        seg(ax, px1, py1, mid_x, py1, lw=LW)
        seg(ax, mid_x, py1, mid_x, py2, lw=LW)
        seg(ax, mid_x, py2, px2, py2, lw=LW)
        ax.annotate('', xy=(px2, py2), xytext=(mid_x, py2),
                    arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=13))
        # подпись у вертикального сегмента
        tx = mid_x - 0.15
        ty = (py1+py2)/2
        txt(ax, tx, ty, lbl, ha='right', fs=8.8)

    # ── Выходы (справа от A4) ──────────────────────────────────
    x4, y4, _, _ = boxes[-1]
    # O1 — успех
    seg(ax, x4+BW, y4+1.3, 19.5, y4+1.3, lw=LW)
    ax.annotate('', xy=(19.5, y4+1.3), xytext=(x4+BW, y4+1.3),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=13))
    txt(ax, 17.8, y4+1.55, 'Успешный просмотр\nтрансляции', fs=9)
    # O2 — ошибка
    seg(ax, x4+BW, y4+0.6, 19.5, y4+0.6, lw=LW)
    ax.annotate('', xy=(19.5, y4+0.6), xytext=(x4+BW, y4+0.6),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=13))
    txt(ax, 17.8, y4+0.35, 'Ошибка воспроизведения', fs=9)

    save(fig, 'clean_02_idef0_decomp.png')


# ══════════════════════════════════════════════════════════════════════════════
#  3. DFD «Просмотр трансляции» — вертикальный поток, линии не пересекаются
# ══════════════════════════════════════════════════════════════════════════════
def gen_dfd():
    W, H = 20, 13
    fig, ax = new_fig(W, H)

    ax.text(W/2, H-0.35, 'DFD — Просмотр трансляции  —  «СвойСпорт TV»',
            ha='center', va='top', fontsize=12, fontweight='bold')

    LW = 1.3

    # ── Вспомогательные функции для DFD-нотации ───────────────
    def ext(cx, cy, w, h, num, name):
        """Внешняя сущность — прямоугольник с тенью"""
        x, y = cx-w/2, cy-h/2
        ax.add_patch(Rectangle((x+SHD, y-SHD), w, h, fc='#CCCCCC', ec='none', zorder=2))
        ax.add_patch(Rectangle((x, y), w, h, fc='white', ec='black', lw=1.5, zorder=3))
        ax.text(x+0.12, y+h-0.14, str(num), fontsize=8, va='top', fontweight='bold', zorder=4)
        for i, ln in enumerate(name.split('\n')):
            oy = (len(name.split('\n'))-1)/2*0.27 - i*0.27
            ax.text(cx, cy+oy, ln, ha='center', va='center',
                    fontsize=10, fontweight='bold', zorder=4)

    def proc(cx, cy, w, h, num, name):
        """Процесс — скруглённый прямоугольник с тенью"""
        x, y = cx-w/2, cy-h/2
        ax.add_patch(Rectangle((x+SHD, y-SHD), w, h, fc='#CCCCCC', ec='none', zorder=2))
        r = FancyBboxPatch((x, y), w, h, boxstyle='round,pad=0.06',
                           fc='#F5F9FF', ec='black', lw=1.4, zorder=3)
        ax.add_patch(r)
        ax.text(x+0.14, y+0.16, '$0', fontsize=7.5, va='bottom', color='#555', zorder=4)
        ax.text(x+w-0.14, y+0.16, str(num), fontsize=8, va='bottom',
                ha='right', fontweight='bold', zorder=4)
        for i, ln in enumerate(name.split('\n')):
            oy = (len(name.split('\n'))-1)/2*0.28 - i*0.28
            ax.text(cx, cy+oy, ln, ha='center', va='center', fontsize=10, zorder=4)

    def store(cx, cy, w, h, num, name):
        """Хранилище данных — открытый прямоугольник"""
        x, y = cx-w/2, cy-h/2
        ax.add_patch(Rectangle((x+SHD, y-SHD), w, h, fc='#CCCCCC', ec='none', zorder=2))
        ax.add_patch(Rectangle((x, y), w, h, fc='#FFFBE6', ec='none', zorder=3))
        ax.plot([x, x+w], [y+h, y+h], 'k-', lw=1.8, zorder=4)
        ax.plot([x, x+w], [y,   y  ], 'k-', lw=1.8, zorder=4)
        ax.plot([x, x],   [y,   y+h], 'k-', lw=1.8, zorder=4)
        ax.plot([x+0.65, x+0.65], [y, y+h], 'k-', lw=1.2, zorder=4)
        ax.text(x+0.33, cy, str(num), ha='center', va='center',
                fontsize=8, fontweight='bold', zorder=4)
        ax.text(x+0.85+(w-0.85)/2, cy, name, ha='center', va='center',
                fontsize=9.5, zorder=4)

    def fl(x1, y1, x2, y2, lbl='', lx=0, ly=0.2, fs=9.0):
        ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
        if lbl:
            ax.text((x1+x2)/2+lx, (y1+y2)/2+ly, lbl, ha='center', va='center',
                    fontsize=fs, bbox=dict(fc='white', ec='none', pad=0.1), zorder=6)

    # ════════════════════════════════════════════════════
    #  КОМПОНОВКА  (вертикальный поток, нет пересечений)
    #
    #   [Зритель]          ←— левая колонка внешних сущностей
    #        |
    #        ↓
    #   [P1: Загрузка каталога]  ←→  [D1: Каталог]  ←→  [Сервер]
    #        |
    #        ↓
    #   [P2: Инициализация сеанса]  ←→  [Сервер трансляций]
    #        |
    #        ↓
    #   [P3: Воспроизведение]  →  [Smart TV устройство]
    #        |
    #        ↓
    #   [P4: Регистрация сеанса]
    #        |
    #        ↓
    #   [D2: История сеансов]
    # ════════════════════════════════════════════════════

    CX = 5.5     # центр процессов
    PW, PH = 3.8, 1.6   # размер процессов
    EW, EH = 2.6, 1.2   # размер внешних сущностей

    # ── Внешние сущности (левый столбец) ──────────────────────
    ext(1.5, 11.0, EW, EH, 1, 'Зритель')

    # ── Процессы (центральный столбец) ───────────────────────
    proc(CX, 9.0, PW, PH, 1, 'Загрузка\nкаталога')
    proc(CX, 6.8, PW, PH, 2, 'Инициализация\nсеанса')
    proc(CX, 4.6, PW, PH, 3, 'Воспроизведение\nтрансляции')
    proc(CX, 2.5, PW, PH, 4, 'Регистрация\nсеанса')

    # ── Хранилища (справа, средний столбец) ───────────────────
    store(12.5, 9.0, 4.4, 0.80, 1, 'Каталог трансляций')
    store(12.5, 2.5, 4.4, 0.80, 2, 'История сеансов')

    # ── Внешние сущности (правый столбец) ────────────────────
    ext(17.5, 9.0, 2.6, EH, 2, 'Сервер\nтрансляций')
    ext(17.5, 6.8, 2.6, EH, 3, 'Сервер\nтрансляций')
    ext(17.5, 4.6, 2.6, EH, 4, 'Smart TV\nустройство')

    # ── Потоки ────────────────────────────────────────────────

    # Зритель → P1 (запрос каталога)
    seg(ax, 1.5, 11.0-EH/2, 1.5, 9.0+PH/2, lw=LW)
    fl(1.5, 9.0+PH/2+0.01, CX-PW/2, 9.0,
       lbl='')
    ax.annotate('', xy=(CX-PW/2, 9.0), xytext=(1.5, 9.0),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, 0.7, 9.0+0.22, 'Запрос\nкаталога', ha='center', fs=8.8)
    seg(ax, 1.5, 11.0-EH/2, 1.5, 9.0, lw=LW)
    dot(ax, 1.5, 9.0)

    # Зритель → P2 (выбор трансляции)
    seg(ax, 1.5, 9.0, 1.5, 6.8, lw=LW)
    ax.annotate('', xy=(CX-PW/2, 6.8), xytext=(1.5, 6.8),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    dot(ax, 1.5, 6.8)
    txt(ax, 0.7, 6.8+0.22, 'Выбор\nтрансляции', ha='center', fs=8.8)

    # Зритель → P3 (команды плеера)
    seg(ax, 1.5, 6.8, 1.5, 4.6, lw=LW)
    ax.annotate('', xy=(CX-PW/2, 4.6), xytext=(1.5, 4.6),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    dot(ax, 1.5, 4.6)
    txt(ax, 0.7, 4.6+0.22, 'Команды\nплеера', ha='center', fs=8.8)

    # P1 → P2 (данные каталога)
    fl(CX, 9.0-PH/2, CX, 6.8+PH/2, 'Данные каталога', lx=0.8)

    # P2 → P3 (параметры потока)
    fl(CX, 6.8-PH/2, CX, 4.6+PH/2, 'URL потока (HLS/DASH)', lx=0.9)

    # P3 → P4 (данные сеанса)
    fl(CX, 4.6-PH/2, CX, 2.5+PH/2, 'Данные сеанса\n(matchId, userId, ts)', lx=1.0)

    # P1 ↔ D1 (каталог)
    fl(CX+PW/2, 9.0, 12.5-4.4/2, 9.0, 'Кэшировать / читать каталог', ly=0.25)
    fl(12.5-4.4/2, 8.7, CX+PW/2, 8.7, 'Данные каталога', ly=-0.25)

    # D1 ↔ Сервер (загрузка)
    fl(12.5+4.4/2, 9.0, 17.5-2.6/2, 9.0, 'HTTP-запрос (SportTV API)', ly=0.25)
    fl(17.5-2.6/2, 8.7, 12.5+4.4/2, 8.7, 'JSON список матчей', ly=-0.25)

    # P2 ↔ Сервер (URL потока)
    fl(CX+PW/2, 6.8, 17.5-2.6/2, 6.8, 'Запрос URL потока (matchId)', ly=0.25)
    fl(17.5-2.6/2, 6.5, CX+PW/2, 6.5, 'URL потока', ly=-0.25)

    # P3 → Smart TV (видеопоток)
    fl(CX+PW/2, 4.6, 17.5-2.6/2, 4.6, 'Видеопоток на экране', ly=0.25)

    # P4 → D2 (история)
    fl(CX, 2.5-PH/2, CX, 2.5-PH/2-0.4, '')
    seg(ax, CX, 2.5-PH/2-0.4, 12.5, 2.5-PH/2-0.4, lw=LW)
    ax.annotate('', xy=(12.5, 2.5-PH/2+0.4), xytext=(12.5, 2.5-PH/2-0.4),
                arrowprops=dict(arrowstyle='->', color='black', lw=LW, mutation_scale=14))
    txt(ax, (CX+12.5)/2, 2.5-PH/2-0.6, 'Запись сеанса просмотра', fs=9)

    save(fig, 'clean_03_dfd.png')


# ══════════════════════════════════════════════════════════════════════════════
if __name__ == '__main__':
    gen_idef0_ctx()
    gen_idef0_decomp()
    gen_dfd()
    print(f'\nФайлы сохранены в: {OUT}')
