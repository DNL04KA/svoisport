"""
Генерация 5 диаграмм для проекта «СвойСпорт TV»
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, Ellipse
import numpy as np
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# ─────────────────────────────────────────────────────────────
# ДИАГРАММА 1 — ВАРИАНТЫ ИСПОЛЬЗОВАНИЯ
# ─────────────────────────────────────────────────────────────
def draw_usecase():
    fig, ax = plt.subplots(figsize=(14, 18))
    fig.patch.set_facecolor('white')
    ax.set_xlim(0, 14); ax.set_ylim(0, 18); ax.axis('off')

    def actor(x, y, name):
        ax.add_patch(plt.Circle((x, y+1.1), 0.32, fc='white', ec='black', lw=1.6, zorder=4))
        ax.plot([x, x],         [y+0.78, y+0.15],  'k-', lw=1.5, zorder=4)
        ax.plot([x-0.42, x+0.42],[y+0.48, y+0.48], 'k-', lw=1.5, zorder=4)
        ax.plot([x, x-0.38],    [y+0.15, y-0.45],  'k-', lw=1.5, zorder=4)
        ax.plot([x, x+0.38],    [y+0.15, y-0.45],  'k-', lw=1.5, zorder=4)
        for i, ln in enumerate(name.split('\n')):
            ax.text(x, y-0.65-i*0.26, ln, ha='center', va='top', fontsize=9,
                    fontweight='bold', zorder=4)

    def uc(cx, cy, w, h, text):
        ax.add_patch(Ellipse((cx, cy), w, h, fc='#EDF3FB', ec='#1A3A5C', lw=1.8, zorder=3))
        for i, ln in enumerate(text.split('\n')):
            off = (len(text.split('\n'))-1)/2*0.22 - i*0.24
            ax.text(cx, cy+off, ln, ha='center', va='center', fontsize=8.5, zorder=4)

    def assoc(ax1, ax2, ay1, ay2):
        ax.plot([ax1, ax2], [ay1, ay2], 'k-', lw=1.0, zorder=2)

    def dep(x1,y1,x2,y2,lbl):
        ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->', lw=1.1, color='#333',
                                   linestyle='dashed'))
        ax.text((x1+x2)/2+0.05, (y1+y2)/2+0.15, lbl, fontsize=7.5,
                ha='center', style='italic', color='#333',
                bbox=dict(fc='white', ec='none', pad=0.1))

    # Заголовок
    ax.text(7, 17.7, 'Диаграмма вариантов использования', ha='center',
            fontsize=13, fontweight='bold')
    ax.text(7, 17.25, 'Система «СвойСпорт TV»', ha='center', fontsize=9.5, color='#444')

    # Граница системы
    ax.add_patch(FancyBboxPatch((2.5, 1.2), 9, 15.3, boxstyle='square,pad=0',
                                fc='none', ec='#1A3A5C', lw=2, zorder=1))
    ax.text(7, 16.75, 'Приложение «СвойСпорт TV»', ha='center',
            fontsize=9, color='#1A3A5C', style='italic')

    # ВИ (cx, cy, w, h, text)
    cases = [
        (7, 15.7, 4.2, 0.80, 'Запустить приложение'),
        (7, 14.4, 4.4, 0.80, 'Аутентификация\n(ввод кода)'),
        (7, 13.0, 4.2, 0.80, 'Просмотреть главный\nэкран'),
        (7, 11.6, 4.2, 0.80, 'Получить список\nтрансляций'),
        (7, 10.2, 4.2, 0.80, 'Выбрать трансляцию'),
        (7,  8.8, 4.2, 0.80, 'Просмотреть детали\nматча'),
        (7,  7.4, 4.2, 0.80, 'Запустить\nвоспроизведение'),
        (7,  6.0, 4.2, 0.80, 'Управлять\nвоспроизведением'),
        (7,  4.6, 4.2, 0.80, 'Просмотреть\nрасписание'),
        (7,  3.2, 4.4, 0.80, 'Получить уведомление\nоб ошибке'),
    ]
    for c in cases:
        uc(*c)

    # Зритель слева
    actor(1.1, 9.0, 'Зритель')
    for (cx, cy, *_) in cases:
        assoc(1.55, cx-2.1, 9.55, cy)

    # Администратор справа
    actor(12.9, 12.5, 'Адми-\nнистратор')
    admin_uc = [
        (7, 15.7, 4.2, 0.80, ''),   # Запустить приложение — тоже
    ]
    # Свои ВИ администратора
    adm_cases = [
        (7, 15.7, None),  # общий с зрителем
        (7, 14.4, None),  # общий
    ]
    for (cx, cy, *_) in cases[:2]:
        assoc(12.45, cx+2.1, 13.1, cy)

    # include / extend
    dep(7, 7.4-0.4, 7, 6.0+0.4, '«include»')  # Запустить→Управлять через include
    dep(7, 8.8-0.4, 7, 7.4+0.4, '«include»')

    # Легенда
    ax.text(0.15, 1.0, 'Обозначения:', fontsize=8, fontweight='bold')
    actor(0.6, -0.1, '')
    ax.text(1.1, 0.2, '— Актор', fontsize=7.5, va='center')
    ax.add_patch(Ellipse((3.0, 0.3), 1.5, 0.45, fc='#EDF3FB', ec='#1A3A5C', lw=1.5))
    ax.text(3.0, 0.3, 'Вариант использования', ha='center', va='center', fontsize=6)
    ax.text(4.0, 0.3, '— ВИ', fontsize=7.5, va='center')

    plt.tight_layout(pad=0.3)
    plt.savefig(f'{OUT}/01_use_case.png', dpi=160, bbox_inches='tight', facecolor='white')
    plt.close()
    print('✓ 01_use_case.png')


# ─────────────────────────────────────────────────────────────
# ДИАГРАММА 2 — СОСТОЯНИЯ СЕАНСА ПРОСМОТРА
# ─────────────────────────────────────────────────────────────
def draw_state():
    fig, ax = plt.subplots(figsize=(14, 19))
    fig.patch.set_facecolor('white')
    ax.set_xlim(0, 14); ax.set_ylim(0, 19); ax.axis('off')

    def state_box(cx, cy, w, h, name, action=''):
        ax.add_patch(FancyBboxPatch((cx-w/2, cy-h/2), w, h,
                                   boxstyle='round,pad=0.12',
                                   fc='#E8EEF6', ec='#1A3A5C', lw=2, zorder=3))
        if action:
            ax.plot([cx-w/2, cx+w/2], [cy+h/2-0.42, cy+h/2-0.42],
                    color='#1A3A5C', lw=1.2, zorder=4)
            ax.text(cx, cy+h/2-0.22, name, ha='center', va='center',
                    fontsize=10, fontweight='bold', zorder=5)
            ax.text(cx, cy-0.10, action, ha='center', va='center',
                    fontsize=8, color='#444', zorder=5, style='italic')
        else:
            ax.text(cx, cy, name, ha='center', va='center',
                    fontsize=10, fontweight='bold', zorder=5)

    def init_pt(cx, cy):
        ax.add_patch(plt.Circle((cx, cy), 0.22, fc='black', ec='black', zorder=5))

    def final_pt(cx, cy):
        ax.add_patch(plt.Circle((cx, cy), 0.32, fc='white', ec='black', lw=2, zorder=4))
        ax.add_patch(plt.Circle((cx, cy), 0.20, fc='black', zorder=5))

    def tr(x1,y1, x2,y2, lbl='', rad=0.0, ldx=0.0, ldy=0.18, fs=8.2):
        ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5,
                                   mutation_scale=16,
                                   connectionstyle=f'arc3,rad={rad}'))
        if lbl:
            mx,my = (x1+x2)/2+ldx, (y1+y2)/2+ldy
            ax.text(mx,my, lbl, ha='center', va='center', fontsize=fs,
                    bbox=dict(fc='white', ec='none', pad=0.15, alpha=0.9), zorder=6)

    ax.text(7, 18.65, 'Диаграмма состояний сеанса просмотра трансляции',
            ha='center', fontsize=12.5, fontweight='bold')
    ax.text(7, 18.15, 'Приложение «СвойСпорт TV»', ha='center', fontsize=9, color='#444')

    # ── узлы ──────────────────────────────────────────────────
    #          (cx,  cy,   w,    h,   name,                action)
    states = [
        (7.0, 17.1, 4.2, 0.80, 'Инициализация',     'entry / запуск PlayerManager'),
        (7.0, 15.3, 4.2, 0.80, 'Загрузка параметров','do / HTTP-запрос URL потока'),
        (7.0, 13.4, 4.2, 0.80, 'Буферизация',        'do / ExoPlayer.prepare()'),
        (4.2, 11.0, 4.0, 0.80, 'Воспроизведение',    'do / ExoPlayer.play()'),
        (4.2,  8.8, 4.0, 0.80, 'Пауза',              'do / ExoPlayer.pause()'),
        (10.5,11.0, 4.0, 0.80, 'Ошибка',             'entry / показать ошибку'),
        (10.5, 8.8, 4.0, 0.80, 'Повтор подключения', 'do / retry (макс. 3 попытки)'),
        (7.0,  6.5, 4.2, 0.80, 'Завершён',           'exit / PlayerManager.release()'),
    ]
    for s in states:
        state_box(*s)

    init_pt(7.0, 17.9)
    final_pt(7.0, 5.3)

    # ── переходы ──────────────────────────────────────────────
    tr(7.0, 17.66, 7.0, 17.50, 'Запуск плеера')
    tr(7.0, 16.70, 7.0, 15.70, 'matchId получен')
    tr(7.0, 14.90, 7.0, 13.80, 'URL потока получен')
    # буферизация → воспроизведение
    tr(5.8, 13.02, 4.6, 11.40, 'Буфер заполнен')
    # воспроизведение ↔ пауза
    tr(4.2, 10.60, 4.2,  9.20, 'Команда «Пауза»',         rad=0.30, ldx=-1.6)
    tr(4.2,  9.20, 4.2, 10.60, 'Команда «Продолжить»',    rad=0.30, ldx= 1.6)
    # воспроизведение → ошибка
    tr(6.2, 11.0,  8.5, 11.0,  'Потеря соединения')
    # буферизация → ошибка
    tr(9.1, 13.4,  10.5,11.40, 'Таймаут / сервер недоступен', ldx=1.5, ldy=0)
    # ошибка → повтор
    tr(10.5,10.60, 10.5, 9.20, 'Повторная попытка',        rad=0.3, ldx=1.8)
    # повтор → буферизация (успех)
    tr(9.1,  8.9,  7.0, 13.02, 'Соединение восстановлено', rad=-0.25, ldx=-1.8, ldy=0.2)
    # пауза → завершён
    tr(4.2,  8.40, 5.8,  6.90, 'Команда «Выход»')
    # воспроизведение → завершён
    tr(4.8, 10.60, 5.8,  6.90, 'Команда «Выход»',          rad=-0.15, ldx=-1.5, ldy=0)
    # ошибка → завершён
    tr(10.0,10.60, 8.2,  6.90, 'Отмена',                   rad=0.15,  ldx=1.2,  ldy=0)
    # повтор → завершён (лимит)
    tr(10.5, 8.40, 8.2,  6.90, 'Превышен лимит попыток',   ldx=1.8,   ldy=0)
    # завершён → final
    tr(7.0,  6.10, 7.0,  5.62, '')

    # note
    ax.text(0.3, 10.2,
            'Внутренние\nдействия:\ndo / обновление\nпрогресс-бара\ndo / контроль буфера',
            fontsize=7.5, color='#333',
            bbox=dict(boxstyle='round,pad=0.35', fc='#FFFFF0', ec='#AAAAAA', alpha=0.95))
    ax.annotate('', xy=(2.2, 11.0), xytext=(1.6, 10.9),
                arrowprops=dict(arrowstyle='->', color='#888', lw=1.0, linestyle='dotted'))

    plt.tight_layout(pad=0.3)
    plt.savefig(f'{OUT}/02_state_diagram.png', dpi=160, bbox_inches='tight', facecolor='white')
    plt.close()
    print('✓ 02_state_diagram.png')


# ─────────────────────────────────────────────────────────────
# ДИАГРАММА 3 — IDEF0 КОНТЕКСТНАЯ A-0
# ─────────────────────────────────────────────────────────────
def draw_idef0_ctx():
    fig, ax = plt.subplots(figsize=(18, 12))
    fig.patch.set_facecolor('white')
    ax.set_xlim(0, 18); ax.set_ylim(0, 12); ax.axis('off')

    def box(cx, cy, w, h, lines):
        ax.add_patch(FancyBboxPatch((cx-w/2, cy-h/2), w, h, boxstyle='square,pad=0',
                                   fc='#E8EEF6', ec='#1A3A5C', lw=2.5, zorder=3))
        for i, ln in enumerate(lines):
            off = (len(lines)-1)/2*0.32 - i*0.36
            ax.text(cx, cy+off, ln, ha='center', va='center',
                    fontsize=11, fontweight='bold', zorder=4)

    def arr(x1,y1,x2,y2, mcolor='#1A3A5C'):
        ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->', color=mcolor, lw=2.0,
                                   mutation_scale=18))

    def lbl(x,y,text, ha='center', fs=9, color='#111'):
        ax.text(x,y,text, ha=ha, va='center', fontsize=fs, color=color,
                bbox=dict(fc='white', ec='none', pad=0.2, alpha=0.9), zorder=5)

    def seg(x1,y1,x2,y2, color='#1A3A5C'):
        ax.plot([x1,x2],[y1,y2], color=color, lw=2.0, zorder=4)

    def dot(x,y, color='#1A3A5C'):
        ax.plot(x,y,'o', color=color, ms=5, zorder=6)

    # Рамка IDEF0
    ax.add_patch(FancyBboxPatch((0.15,0.15), 17.7, 11.5, boxstyle='square,pad=0',
                                fc='none', ec='#333', lw=2, zorder=0))

    # Заголовок
    ax.text(9, 11.45, 'Диаграмма IDEF0 — Контекстная диаграмма (A-0)',
            ha='center', fontsize=13, fontweight='bold')
    ax.text(9, 11.00, 'Система: «СвойСпорт TV»   Назначение: просмотр спортивных '
                      'трансляций на Smart TV   Дата: 2026-05-24',
            ha='center', fontsize=8.5, color='#444')

    # Главная функция
    box(9, 6.0, 6.2, 3.6, ['A-0', '', 'Обеспечить просмотр', 'спортивных трансляций', 'на Smart TV устройстве'])

    # ── ВХОДЫ (слева) ──
    # I1 + I2 объединены через тройник
    seg(0.5, 7.1, 2.0, 7.1)
    seg(0.5, 5.3, 2.0, 5.3)
    seg(0.5, 6.2, 2.0, 6.2)
    seg(2.0, 5.3, 2.0, 7.1)
    dot(2.0, 7.1); dot(2.0, 5.3); dot(2.0, 6.2)
    arr(2.0, 6.2, 5.9, 6.2)
    lbl(1.1, 7.35, 'I1: Запросы пользователя\n(навигация, выбор)', fs=8.2)
    lbl(1.1, 5.05, 'I2: Команды управления\nплеером (пауза, выход)', fs=8.2)
    lbl(0.3, 6.2, 'ВХОД', ha='center', fs=7.5, color='#1A3A5C')

    # ── ВЫХОДЫ (справа) ──
    arr(12.1, 7.0, 15.5, 7.4)
    arr(12.1, 6.0, 15.5, 6.0)
    arr(12.1, 5.0, 15.5, 4.6)
    seg(15.5, 4.6, 15.5, 7.4)
    dot(15.5, 4.6); dot(15.5, 6.0); dot(15.5, 7.4)
    seg(15.5, 6.0, 17.0, 6.0)
    lbl(16.1, 6.0, 'ВЫХОД', fs=7.5, color='#1A3A5C')
    lbl(16.5, 7.4, 'O1: Видеопоток\nна экране (HLS)', fs=8.2)
    lbl(16.5, 6.0, 'O2: UI каталога\nи навигации', fs=8.2)
    lbl(16.5, 4.6, 'O3: Уведомления\nоб ошибках', fs=8.2)

    # ── УПРАВЛЕНИЕ (сверху) ──
    arr(7.0, 10.2, 7.8, 7.8)
    arr(9.0, 10.4, 9.0, 7.8)
    arr(11.0,10.2,10.2, 7.8)
    seg(7.0, 10.2, 11.0, 10.2)
    dot(7.0,10.2); dot(9.0,10.2); dot(11.0,10.2)
    seg(9.0, 10.2, 9.0, 10.8)
    lbl(9.0, 11.0, 'УПРАВЛЕНИЕ', fs=7.5, color='#1A3A5C')
    lbl(6.2, 10.65, 'C1: Требования к качеству\nвидео (битрейт, разрешение)', fs=8.2)
    lbl(9.0, 10.75, 'C2: Правила доступа\nк трансляциям', fs=8.2)
    lbl(11.8,10.65, 'C3: Протоколы\nHLS / DASH', fs=8.2)

    # ── МЕХАНИЗМЫ (снизу) ──
    arr(7.0, 3.1, 7.5, 4.2, mcolor='#444')
    arr(9.0, 3.1, 9.0, 4.2, mcolor='#444')
    arr(11.0,3.1,10.5, 4.2, mcolor='#444')
    seg(7.0,3.1,11.0,3.1, color='#444')
    dot(7.0,3.1); dot(9.0,3.1); dot(11.0,3.1)
    seg(9.0, 3.1, 9.0, 2.4, color='#444')
    lbl(9.0, 2.1, 'МЕХАНИЗМЫ', fs=7.5, color='#444')
    lbl(6.5,  2.8, 'M1: Android TV OS\n+ ExoPlayer', fs=8.2, color='#333')
    lbl(9.0,  2.8, 'M2: Сервер трансляций\n(SportTV API)', fs=8.2, color='#333')
    lbl(11.5, 2.8, 'M3: Smart TV\nустройство', fs=8.2, color='#333')

    # Нижняя рамка-паспорт
    ax.add_patch(FancyBboxPatch((0.2, 0.2), 17.6, 1.5, boxstyle='square,pad=0',
                                fc='#F8F8F8', ec='#999', lw=1, zorder=1))
    fields = [
        ('Автор:', 'СвойСпорт команда'), ('Проект:', 'ProgramOnTV'),
        ('Диаграмма:', 'A-0 Контекстная'), ('Стандарт:', 'IDEF0 / ГОСТ Р ИСО 10746'),
        ('Дата:', '2026-05-24'),
    ]
    for i, (k, v) in enumerate(fields):
        ax.text(0.4 + i*3.5, 1.1, k, fontsize=7.5, fontweight='bold')
        ax.text(0.4 + i*3.5, 0.7, v, fontsize=7.5, color='#333')

    plt.tight_layout(pad=0.2)
    plt.savefig(f'{OUT}/03_idef0_context.png', dpi=160, bbox_inches='tight', facecolor='white')
    plt.close()
    print('✓ 03_idef0_context.png')


# ─────────────────────────────────────────────────────────────
# ДИАГРАММА 4 — IDEF0 ДЕКОМПОЗИЦИЯ A0
# ─────────────────────────────────────────────────────────────
def draw_idef0_decomp():
    fig, ax = plt.subplots(figsize=(20, 14))
    fig.patch.set_facecolor('white')
    ax.set_xlim(0, 20); ax.set_ylim(0, 14); ax.axis('off')

    def fbox(cx, cy, w, h, num, lines):
        ax.add_patch(FancyBboxPatch((cx-w/2, cy-h/2), w, h, boxstyle='square,pad=0',
                                   fc='#E8EEF6', ec='#1A3A5C', lw=2, zorder=3))
        ax.text(cx+w/2-0.12, cy-h/2+0.12, num, ha='right', va='bottom',
                fontsize=7.5, color='#555', style='italic', zorder=4)
        for i, ln in enumerate(lines):
            off = (len(lines)-1)/2*0.28 - i*0.30
            ax.text(cx, cy+off+0.05, ln, ha='center', va='center',
                    fontsize=9.5, fontweight='bold', zorder=4)

    def arr(x1,y1,x2,y2, color='#1A3A5C'):
        ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->', color=color, lw=1.7,
                                   mutation_scale=15))

    def seg(x1,y1,x2,y2, color='#1A3A5C'):
        ax.plot([x1,x2],[y1,y2], color=color, lw=1.7, zorder=4)

    def dot(x,y, color='#1A3A5C'):
        ax.plot(x,y,'o', color=color, ms=4.5, zorder=6)

    def lbl(x, y, text, ha='center', fs=8.0, color='#111'):
        ax.text(x, y, text, ha=ha, va='center', fontsize=fs, color=color,
                bbox=dict(fc='white', ec='none', pad=0.15, alpha=0.92), zorder=5)

    # Рамка + заголовок
    ax.add_patch(FancyBboxPatch((0.1,0.1),19.8,13.7, boxstyle='square,pad=0',
                                fc='none', ec='#333', lw=2, zorder=0))
    ax.text(10, 13.6, 'Диаграмма IDEF0 — Декомпозиция (A0)',
            ha='center', fontsize=13, fontweight='bold')
    ax.text(10, 13.15, 'Родительская: A-0  |  Система: «СвойСпорт TV»  |  Дата: 2026-05-24',
            ha='center', fontsize=8.5, color='#444')

    # ── Функциональные блоки ──────────────────────────────────
    #  A1 (4.0, 11.0)   A2 (12.5, 11.0)
    #  A3 (4.0,  7.0)   A4 (12.5,  7.0)   A5 (18.0, 9.0)
    fbox( 4.0, 11.0, 5.0, 2.0, 'A1', ['Аутентификация', 'пользователя'])
    fbox(12.5, 11.0, 5.0, 2.0, 'A2', ['Загрузка каталога', 'трансляций'])
    fbox( 4.0,  7.0, 5.0, 2.0, 'A3', ['Просмотр', 'трансляции'])
    fbox(12.5,  7.0, 5.0, 2.0, 'A4', ['Регистрация', 'сеанса просмотра'])
    fbox(17.5,  9.0, 4.2, 2.0, 'A5', ['Обработка', 'ошибок'])

    # ── ВХОДЫ (левая граница) ──────────────────────────────────
    # I1 → A1  (данные для входа)
    arr(0.3, 11.2, 1.5, 11.2)
    lbl(0.85, 11.5, 'I1: Данные\nаутентификации', fs=7.8)

    # I2 → A2  (запрос каталога)
    arr(0.3, 11.0, 1.5, 11.0)   # branch of same input line
    seg(0.3, 10.6, 0.3, 11.5)
    seg(0.3, 10.6, 1.5, 10.6)
    arr(1.5, 10.6, 1.5, 10.8)   # split to A1 lower
    arr(0.3, 10.6, 0.3, 11.0)
    dot(0.3, 10.6)

    # I2 → A3 (команды управления плеером)
    arr(0.3, 7.2, 1.5, 7.2)
    lbl(0.85, 7.55, 'I2: Команды\nуправления', fs=7.8)

    # ── ВНУТРЕННИЕ ПОТОКИ ──────────────────────────────────────
    # A1 → A2 : Токен доступа
    arr(6.5, 11.2, 10.0, 11.2)
    lbl(8.25, 11.50, 'Токен доступа', fs=8.0)

    # A1 → A3 : через вертикальный провод
    seg(4.0, 10.0, 4.0, 8.0)
    arr(4.0,  8.0, 4.0, 8.0)   # just a dot, arrow below
    ax.annotate('', xy=(4.0, 8.0), xytext=(4.0, 10.0),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(4.65, 9.0, 'Разрешение\nна просмотр', fs=7.8)

    # A2 → A3 : URL потока (вниз через провод)
    seg(12.5, 10.0, 12.5, 8.5)
    seg(12.5,  8.5,  6.5,  8.5)
    arr(6.5, 8.5, 6.5, 8.0)
    lbl(9.5, 8.75, 'URL потока (HLS/DASH)', fs=8.0)

    # A2 → A4 : список трансляций выбранная
    seg(15.0, 10.0, 15.0,  9.0)
    arr(15.0,  9.0, 15.0,  8.0)
    lbl(15.85, 9.0, 'Выбранная\nтрансляция', fs=7.8)

    # A3 → A4 : данные сеанса
    arr(6.5, 7.0, 10.0, 7.0)
    lbl(8.25, 7.35, 'Данные сеанса\n(matchId, время)', fs=7.8)

    # A3 → A5 : ошибки воспроизведения
    seg(6.5, 6.5, 6.5, 5.2)
    seg(6.5, 5.2, 17.5, 5.2)
    arr(17.5, 5.2, 17.5, 8.0)
    lbl(12.0, 5.0, 'Ошибки воспроизведения', fs=8.0)

    # A5 → A3 : команда повтора
    seg(17.5, 10.0, 17.5, 11.8)
    seg(17.5, 11.8, 4.0, 11.8)
    seg(4.0, 11.8, 4.0, 12.0)    # до управляющей черты (C)
    # или вернём ниже к A3
    seg(16.0,  9.0,  7.5,  9.0)
    arr(7.5,   9.0,  7.5,  8.0)
    lbl(12.0,  9.3,  'Команда повтора / отмены', fs=8.0)
    dot(16.0, 9.0); dot(17.5, 9.0)
    seg(16.0, 9.0, 17.5, 9.0)

    # ── ВЫХОДЫ (правая граница) ────────────────────────────────
    # A3 → Видеопоток
    arr(6.5, 7.3, 10.0, 7.3)   # временно; вынесем правее
    seg(6.5, 7.3, 6.5, 7.3)
    ax.annotate('', xy=(19.7, 7.3), xytext=(6.5, 7.3),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(13.0, 7.55, 'O1: Видеопоток на экране', fs=8.0)

    # A4 → История сеансов
    ax.annotate('', xy=(19.7, 6.7), xytext=(15.0, 6.7),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(17.3, 6.45, 'O2: История\nпросмотра', fs=8.0)

    # A5 → Уведомление
    ax.annotate('', xy=(19.7, 8.6), xytext=(19.6, 8.6),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    seg(19.6, 8.6, 19.6, 8.6)
    # надпись
    ax.annotate('', xy=(19.7, 10.2), xytext=(15.0, 10.2),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(17.3, 10.45, 'O3: Каталог\nна экране', fs=8.0)

    # ── УПРАВЛЕНИЕ (сверху) ────────────────────────────────────
    # C1 → A1
    ax.annotate('', xy=(4.0, 12.0), xytext=(4.0, 13.0),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(3.5, 13.1, 'C1: Правила\nаутентификации', fs=7.8)

    # C2 → A2
    ax.annotate('', xy=(12.5, 12.0), xytext=(12.5, 13.0),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    lbl(13.0, 13.1, 'C2: Правила доступа\nк контенту', fs=7.8)

    # C3 → A3
    ax.annotate('', xy=(4.0, 8.0), xytext=(2.8, 8.0),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.7, mutation_scale=15))
    seg(2.8, 8.0, 2.8, 13.2)
    seg(2.8, 13.2, 4.0, 13.2)
    lbl(2.2, 10.5, 'C3: Протокол\nHLS/DASH', fs=7.8)

    # ── МЕХАНИЗМЫ (снизу) ─────────────────────────────────────
    for mx, name in [(4.0,'M1: Android TV +\nExoPlayer'), (12.5,'M2: SportTV API'),
                     (17.5,'M3: Smart TV\nустройство')]:
        ax.annotate('', xy=(mx, 6.0), xytext=(mx, 5.0),
                    arrowprops=dict(arrowstyle='->', color='#555', lw=1.5, mutation_scale=14))
        lbl(mx, 4.7, name, fs=7.8, color='#444')

    # M4 → A4
    ax.annotate('', xy=(12.5, 6.0), xytext=(12.5, 5.0),
                arrowprops=dict(arrowstyle='->', color='#555', lw=1.5, mutation_scale=14))

    # Паспорт
    ax.add_patch(FancyBboxPatch((0.15, 0.15), 19.7, 1.2, boxstyle='square,pad=0',
                                fc='#F8F8F8', ec='#999', lw=1, zorder=1))
    for i,(k,v) in enumerate([('Автор:','СвойСпорт команда'),('Проект:','ProgramOnTV'),
                               ('Диаграмма:','A0 Декомпозиция'),('Стандарт:','IDEF0'),
                               ('Дата:','2026-05-24')]):
        ax.text(0.4+i*3.9, 1.05, k, fontsize=7.5, fontweight='bold')
        ax.text(0.4+i*3.9, 0.65, v, fontsize=7.5, color='#333')

    plt.tight_layout(pad=0.2)
    plt.savefig(f'{OUT}/04_idef0_decomp.png', dpi=160, bbox_inches='tight', facecolor='white')
    plt.close()
    print('✓ 04_idef0_decomp.png')


# ─────────────────────────────────────────────────────────────
# ДИАГРАММА 5 — DFD «Просмотр трансляции»
# ─────────────────────────────────────────────────────────────
def draw_dfd():
    fig, ax = plt.subplots(figsize=(20, 15))
    fig.patch.set_facecolor('white')
    ax.set_xlim(0, 20); ax.set_ylim(0, 15); ax.axis('off')

    def proc(cx, cy, r, num, lines):
        """Круг = Процесс (нотация Йордона)"""
        ax.add_patch(plt.Circle((cx,cy), r, fc='#D6E4F0', ec='#1A3A5C', lw=2, zorder=3))
        theta = np.linspace(0.22*np.pi, 0.78*np.pi, 80)
        ax.plot(cx+r*np.cos(theta), cy+r*np.sin(theta), color='#1A3A5C', lw=1.3, zorder=4)
        ax.text(cx, cy+r*0.58, num, ha='center', va='center',
                fontsize=7.5, fontweight='bold', color='#1A3A5C', zorder=5)
        for i,ln in enumerate(lines):
            off = (len(lines)-1)/2*0.23 - i*0.25
            ax.text(cx, cy-0.05+off, ln, ha='center', va='center', fontsize=8.5, zorder=5)

    def ext(cx, cy, w, h, lines):
        """Прямоугольник = Внешняя сущность"""
        ax.add_patch(FancyBboxPatch((cx-w/2, cy-h/2), w, h, boxstyle='square,pad=0',
                                   fc='#F0F0F0', ec='#333', lw=2, zorder=3))
        for i,ln in enumerate(lines):
            off = (len(lines)-1)/2*0.25 - i*0.28
            ax.text(cx, cy+off, ln, ha='center', va='center',
                    fontsize=9, fontweight='bold', zorder=4)

    def store(cx, cy, w, h, num, name):
        """Открытый прямоугольник (два горизонтальных) = Хранилище"""
        ax.add_patch(FancyBboxPatch((cx-w/2, cy-h/2+0.02), w, h-0.04,
                                   boxstyle='square,pad=0',
                                   fc='#FFFBE6', ec='none', lw=0, zorder=2))
        ax.plot([cx-w/2, cx+w/2], [cy+h/2, cy+h/2], color='#1A3A5C', lw=1.8, zorder=3)
        ax.plot([cx-w/2, cx+w/2], [cy-h/2, cy-h/2], color='#1A3A5C', lw=1.8, zorder=3)
        ax.plot([cx-w/2, cx-w/2], [cy-h/2, cy+h/2], color='#1A3A5C', lw=1.8, zorder=3)
        ax.text(cx-w/2+0.22, cy, num, ha='left', va='center',
                fontsize=7.5, fontweight='bold', color='#555', zorder=4)
        ax.plot([cx-w/2+0.65, cx-w/2+0.65], [cy-h/2, cy+h/2],
                color='#bbb', lw=1.0, ls='--', zorder=3)
        ax.text(cx-w/2+0.80, cy, name, ha='left', va='center', fontsize=8.5, zorder=4)

    def flow(x1,y1,x2,y2, lbl='', rad=0.0, ldx=0.0, ldy=0.17, fs=7.8):
        ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5,
                                   mutation_scale=14,
                                   connectionstyle=f'arc3,rad={rad}'))
        if lbl:
            mx,my = (x1+x2)/2+ldx, (y1+y2)/2+ldy
            ax.text(mx,my, lbl, ha='center', va='center', fontsize=fs,
                    bbox=dict(fc='white', ec='none', pad=0.12, alpha=0.9), zorder=6)

    def seg(x1,y1,x2,y2):
        ax.plot([x1,x2],[y1,y2], color='#1A3A5C', lw=1.5, zorder=4)

    def dot(x,y):
        ax.plot(x,y,'o', color='#1A3A5C', ms=4.5, zorder=6)

    # Рамка + заголовок
    ax.add_patch(FancyBboxPatch((0.1,0.1), 19.8, 14.7, boxstyle='square,pad=0',
                                fc='none', ec='#333', lw=2, zorder=0))
    ax.text(10, 14.6, 'DFD — Диаграмма потоков данных',
            ha='center', fontsize=13, fontweight='bold')
    ax.text(10, 14.1, 'Процесс: «Просмотр трансляции»  |  Нотация: Yourdon–DeMarco  |  Дата: 2026-05-24',
            ha='center', fontsize=8.5, color='#444')

    # ── Внешние сущности ──────────────────────────────────────
    ext( 1.5, 12.5, 2.4, 1.1, ['Зритель', '(Пользователь)'])
    ext(18.5, 12.5, 2.4, 1.1, ['Smart TV', 'устройство'])
    ext(18.5,  3.5, 2.4, 1.1, ['Сервер', 'трансляций'])

    # ── Хранилища данных ──────────────────────────────────────
    store(10.0, 13.3, 5.4, 0.65, 'D1', 'Каталог трансляций')
    store( 5.5,  4.0, 5.0, 0.65, 'D2', 'Кэш каталога')
    store(14.5,  4.0, 5.0, 0.65, 'D3', 'История сеансов')

    # ── Процессы ──────────────────────────────────────────────
    proc( 5.5, 11.5, 1.4, 'P1', ['Загрузка', 'каталога'])
    proc( 5.5,  7.5, 1.4, 'P2', ['Формирование', 'UI каталога'])
    proc(10.0, 11.5, 1.4, 'P3', ['Инициализация', 'сеанса'])
    proc(10.0,  7.0, 1.5, 'P4', ['Управление', 'воспроизведением'])
    proc(14.5,  7.0, 1.4, 'P5', ['Обработка', 'ошибок'])
    proc(14.5, 11.5, 1.4, 'P6', ['Регистрация', 'сеанса'])

    # ── Потоки данных ─────────────────────────────────────────
    # Зритель → P1
    flow(2.72, 12.5, 4.1, 11.8, 'Запрос каталога')
    # Зритель → P3 (выбор)
    flow(2.72, 12.2, 8.6, 11.8, 'Выбор трансляции\n(matchId)', rad=-0.15, ldy=0.22)
    # Зритель → P2 (навигация)
    flow(2.72, 12.0, 4.1, 8.0, 'Команды\nнавигации', rad=0.15, ldx=-0.6, ldy=0)
    # Зритель → P4 (команды плеера)
    flow(2.72, 11.8, 8.5, 7.5, 'Команды\nплеера', rad=0.2, ldx=-0.5, ldy=-0.1)

    # P1 ↔ Сервер трансляций
    flow( 6.9, 11.7, 17.3, 12.3, 'HTTP-запрос (SportTV API)', rad=-0.08, ldy=0.22)
    flow(17.3, 12.0,  6.9, 11.3, 'JSON: список матчей',       rad=-0.08, ldy=-0.25)

    # P1 → D1
    flow(6.2, 12.4, 7.7, 13.15, 'Сохранить\nданные каталога', ldx=0.3, ldy=0.2)
    # D1 → P1
    flow(7.7, 12.95, 6.2, 12.1, 'Данные каталога\n(кэш)', ldx=-0.5, ldy=-0.2)

    # P1 → P2
    flow(5.5, 10.1, 5.5, 8.9, 'Данные каталога')

    # P1 → D2 (кэш)
    seg(5.0, 10.1, 5.0, 4.32)
    dot(5.0, 10.1)
    ax.annotate('', xy=(5.0, 4.32), xytext=(5.0, 5.5),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5, mutation_scale=14))
    ax.text(4.2, 7.2, 'Кэшировать\nкаталог', ha='center', va='center', fontsize=7.8,
            bbox=dict(fc='white', ec='none', pad=0.1, alpha=0.9), zorder=5)

    # D2 → P2
    flow(5.5, 4.32, 5.5, 6.1, 'Данные из кэша')

    # P2 → Smart TV (экран каталога)
    flow(6.9, 7.5, 17.3, 12.0, 'Экран каталога\n(Compose UI)', rad=0.08, ldx=0.5, ldy=0.2)

    # P3 ↔ Сервер (URL потока)
    flow(11.4, 11.7, 17.3, 11.8, 'Запрос URL потока\n(matchId)', rad=-0.1, ldy=0.22)
    flow(17.3, 11.5, 11.4, 11.3, 'URL потока\n(HLS/DASH)',       rad=-0.1, ldy=-0.25)

    # P3 → P4
    flow(10.0, 10.1, 10.0, 8.5, 'Параметры потока\n(URL, title, isLive)')

    # P3 → P6
    flow(11.4, 11.5, 13.1, 11.5, 'Данные сеанса\n(matchId, userId, ts)')

    # P6 → D3
    flow(14.5, 10.1, 14.5, 4.32, 'Запись сеанса')
    # D3 → P6
    seg(15.2, 4.32, 15.2, 10.5)
    dot(15.2, 4.32)
    ax.annotate('', xy=(15.9, 10.5), xytext=(15.2, 10.5),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5, mutation_scale=14))
    ax.annotate('', xy=(15.9, 10.5), xytext=(15.9, 10.5),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5, mutation_scale=14))
    # сокращённо — стрелка от D3 вверх к P6
    flow(15.5, 4.32, 15.5, 10.1, 'Подтверждение\nзаписи', ldx=0.6)

    # P4 → Smart TV
    flow(11.5, 7.0, 17.3, 11.5, 'Видеопоток\nна экране', rad=0.2, ldx=0.8, ldy=-0.1)

    # P4 → P5
    flow(11.5, 7.0, 13.1, 7.0, 'Событие ошибки\n(код, тип)')

    # P5 → P4 (повтор)
    flow(13.1, 6.6, 11.5, 6.6, 'Команда\nповтора', rad=-0.15, ldy=-0.25)

    # P5 → Smart TV (уведомление)
    flow(15.9, 7.0, 17.3, 11.2, 'Уведомление\nоб ошибке', rad=0.1, ldx=0.5, ldy=0)

    # ── Легенда ───────────────────────────────────────────────
    lx, ly = 0.3, 3.8
    ax.text(lx, ly, 'Обозначения:', fontsize=8.5, fontweight='bold')
    ax.add_patch(plt.Circle((lx+0.45, ly-0.6), 0.32, fc='#D6E4F0', ec='#1A3A5C', lw=1.5))
    ax.text(lx+0.45, ly-0.6, 'P', ha='center', va='center', fontsize=7.5)
    ax.text(lx+0.9,  ly-0.6, '— Процесс', fontsize=7.5, va='center')

    ax.add_patch(FancyBboxPatch((lx+0.1, ly-1.35), 0.7, 0.45, boxstyle='square,pad=0',
                                fc='#F0F0F0', ec='#333', lw=1.5))
    ax.text(lx+0.45, ly-1.12, 'E', ha='center', va='center', fontsize=7.5)
    ax.text(lx+0.9,  ly-1.12, '— Внешняя сущность', fontsize=7.5, va='center')

    ax.plot([lx+0.1, lx+0.8], [ly-1.65, ly-1.65], color='#1A3A5C', lw=1.5)
    ax.plot([lx+0.1, lx+0.8], [ly-2.0,  ly-2.0],  color='#1A3A5C', lw=1.5)
    ax.plot([lx+0.1, lx+0.1], [ly-2.0,  ly-1.65], color='#1A3A5C', lw=1.5)
    ax.text(lx+0.9, ly-1.82, '— Хранилище данных', fontsize=7.5, va='center')

    ax.annotate('', xy=(lx+0.8, ly-2.4), xytext=(lx+0.1, ly-2.4),
                arrowprops=dict(arrowstyle='->', color='#1A3A5C', lw=1.5, mutation_scale=12))
    ax.text(lx+0.9, ly-2.4, '— Поток данных', fontsize=7.5, va='center')

    plt.tight_layout(pad=0.2)
    plt.savefig(f'{OUT}/05_dfd.png', dpi=160, bbox_inches='tight', facecolor='white')
    plt.close()
    print('✓ 05_dfd.png')


# ─────────────────────────────────────────────────────────────
if __name__ == '__main__':
    draw_usecase()
    draw_state()
    draw_idef0_ctx()
    draw_idef0_decomp()
    draw_dfd()
    print('\nВсе диаграммы сохранены в:', OUT)
