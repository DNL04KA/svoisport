#!/usr/bin/env python3
"""BPWin-style diagram generator — «СвойСпорт TV»"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Rectangle
import numpy as np, os

OUT = os.path.dirname(os.path.abspath(__file__))

# ═══════════════════════════════════════════════════════════
#  BPWin-рамка (header + footer)
# ═══════════════════════════════════════════════════════════
def bpwin_frame(ax, W, H,
                author='СвойСпорт команда', project='ProgramOnTV',
                date='2026-05-24', rev='2026-05-24',
                node='A-0', title='', number='', context='A-0'):
    HDR = 2.2   # высота шапки
    FTR = 1.0   # высота подвала
    lw  = 1.2

    # внешняя рамка
    ax.add_patch(Rectangle((0,0), W, H, fc='white', ec='black', lw=2, zorder=0))

    # ── Шапка ─────────────────────────────────────────────
    hy = H - HDR   # нижняя граница шапки
    ax.plot([0,W],[hy,hy],'k-',lw=lw)

    # вертикальные разделители шапки
    # x: |USEAT|0| AUTHOR/PROJECT |8| DATE |11| STATUS |14.5| READER |16| DATE |17.5| CTX |W
    vl = [3, 8, 11, 14.5, 16, 17.5]
    for x in vl:
        ax.plot([x,x],[hy,H],'k-',lw=lw)

    # горизонтальные внутри STATUS (каждые ~0.48)
    for i in range(1,4):
        y_s = H - 0.08 - i*0.50
        ax.plot([11,14.5],[y_s,y_s],'k-',lw=0.6)

    # USED AT
    ax.text(0.12, H-0.12, 'USED AT:', fontsize=7.5, fontweight='bold', va='top')
    ax.text(0.12, hy+0.22, 'NOTES:  1  2  3  4  5  6  7  8  9  10',
            fontsize=6.5, va='bottom')

    # AUTHOR / PROJECT
    ax.text(3.15, H-0.12, f'AUTHOR:  {author}', fontsize=8, va='top')
    ax.text(3.15, H-0.62, f'PROJECT:  {project}', fontsize=8, va='top')

    # DATE / REV
    ax.text(8.15, H-0.12, f'DATE:  {date}', fontsize=8, va='top')
    ax.text(8.15, H-0.62, f'REV:    {rev}',  fontsize=8, va='top')

    # STATUS
    for i, s in enumerate(['WORKING','DRAFT','RECOMMENDED','PUBLICATION']):
        ax.text(11.15, H-0.12-i*0.50, s, fontsize=7.5, va='top')

    # READER / DATE (column headers)
    ax.text(14.6, H-0.12, 'READER', fontsize=7.5, fontweight='bold', va='top')
    ax.text(16.1, H-0.12, 'DATE',   fontsize=7.5, fontweight='bold', va='top')

    # CONTEXT
    ax.text(17.6, H-0.12, 'CONTEXT:', fontsize=7.5, fontweight='bold', va='top')
    # чёрный прямоугольник — текущая диаграмма
    ax.add_patch(Rectangle((W-1.1, hy+0.55), 0.80, 0.58,
                            fc='black', ec='black', zorder=5))
    # код контекста внизу
    ax.text(W-0.70, hy+0.18, context, fontsize=9, ha='center', va='bottom', fontweight='bold')

    # ── Подвал ────────────────────────────────────────────
    ax.plot([0,W],[FTR,FTR],'k-',lw=lw)
    ax.plot([4,4],[0,FTR],'k-',lw=lw)
    ax.plot([W-3.5,W-3.5],[0,FTR],'k-',lw=lw)

    ax.text(0.12, FTR-0.08, 'NODE:',   fontsize=7.5, fontweight='bold', va='top')
    ax.text(2.0,  0.18,     node,       fontsize=13,  fontweight='bold', ha='center')
    ax.text(4.15, FTR-0.08, 'TITLE:',  fontsize=7.5, fontweight='bold', va='top')
    if title:
        ax.text((4+W-3.5)/2, FTR/2, title, fontsize=10, ha='center', va='center')
    ax.text(W-3.4, FTR-0.08,'NUMBER:', fontsize=7.5, fontweight='bold', va='top')
    if number:
        ax.text(W-1.75, FTR/2, number, fontsize=10, ha='center', va='center')

    return hy, FTR   # content_top, content_bottom

def new_fig(W=20, H=14):
    fig, ax = plt.subplots(figsize=(W, H))
    ax.set_xlim(0,W); ax.set_ylim(0,H); ax.axis('off')
    fig.patch.set_facecolor('white')
    return fig, ax

def save(fig, name):
    plt.savefig(f'{OUT}/{name}', dpi=180, bbox_inches='tight', facecolor='white')
    plt.close(fig)
    print(f'✓ {name}')

# ─── shape helpers ──────────────────────────────────────────
SHD = 0.13   # shadow offset

def shadow(ax, x, y, w, h, fc='white', ec='black', lw=1.5, r=0, zb=3):
    """прямоугольник с тенью"""
    bs = f'round,pad={r}' if r else 'square,pad=0'
    ax.add_patch(FancyBboxPatch((x+SHD, y-SHD), w, h, boxstyle=bs,
                                fc='#AAAAAA', ec='none', zorder=zb-1))
    ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle=bs,
                                fc=fc, ec=ec, lw=lw, zorder=zb))

def ext_entity(ax, cx, cy, w, h, num, name):
    """Внешняя сущность — прямоугольник с тенью"""
    x,y = cx-w/2, cy-h/2
    shadow(ax, x, y, w, h, r=0)
    # номер
    ax.text(x+0.10, y+h-0.10, str(num), fontsize=8, va='top', fontweight='bold', zorder=5)
    for i,ln in enumerate(name.split('\n')):
        off = (len(name.split('\n'))-1)/2*0.25 - i*0.27
        ax.text(cx, cy+off, ln, ha='center', va='center', fontsize=10, fontweight='bold', zorder=5)

def process_box(ax, cx, cy, w, h, num, name, ref='$0'):
    """Процесс — скруглённый прямоугольник с тенью, номер справа вверху"""
    x,y = cx-w/2, cy-h/2
    shadow(ax, x, y, w, h, r=0.08)
    # $0 слева вверху
    ax.text(x+0.12, y+h-0.12, ref, fontsize=8, va='top', color='#333', zorder=5)
    # номер справа вверху
    ax.text(x+w-0.12, y+h-0.12, str(num), fontsize=8, va='top', ha='right',
            fontweight='bold', zorder=5)
    for i,ln in enumerate(name.split('\n')):
        off = (len(name.split('\n'))-1)/2*0.28 - i*0.30
        ax.text(cx, cy+off, ln, ha='center', va='center', fontsize=10, zorder=5)

def data_store(ax, cx, cy, w, h, num, name):
    """Хранилище данных — BPWin-style (открытый прямоугольник с тенью)"""
    x,y = cx-w/2, cy-h/2
    # тень
    ax.add_patch(Rectangle((x+SHD, y-SHD), w, h,
                            fc='#AAAAAA', ec='none', zorder=2))
    ax.add_patch(Rectangle((x, y), w, h, fc='white', ec='none', zorder=3))
    # три стороны (открыт справа)
    ax.plot([x,x+w],[y+h,y+h],'k-',lw=2.0,zorder=4)  # верх
    ax.plot([x,x+w],[y,y],    'k-',lw=2.0,zorder=4)  # низ
    ax.plot([x,x],  [y,y+h],  'k-',lw=2.0,zorder=4)  # лево
    # разделитель номера
    ax.plot([x+0.7,x+0.7],[y,y+h],'k-',lw=1.5,zorder=4)
    # номер
    ax.text(x+0.35, cy, str(num), ha='center', va='center',
            fontsize=9, fontweight='bold', zorder=5)
    ax.text(x+0.9+( w-0.9)/2, cy, name, ha='center', va='center',
            fontsize=9.5, zorder=5)

def idef_box(ax, cx, cy, w, h, num, lines):
    """IDEF0 функциональный блок"""
    x,y=cx-w/2,cy-h/2
    shadow(ax,x,y,w,h,r=0)
    # номер — правый нижний угол
    ax.text(x+w-0.12, y+0.12, num, ha='right', va='bottom',
            fontsize=8, color='#444', style='italic', zorder=5)
    for i,ln in enumerate(lines):
        off=(len(lines)-1)/2*0.32-i*0.32
        ax.text(cx,cy+off,ln,ha='center',va='center',fontsize=11,fontweight='bold',zorder=5)

def fl(ax,x1,y1,x2,y2,lbl='',rad=0,lx=0,ly=.18,fs=8.5,lw=1.3):
    ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                arrowprops=dict(arrowstyle='->',color='black',lw=lw,
                                connectionstyle=f'arc3,rad={rad}'))
    if lbl:
        ax.text((x1+x2)/2+lx,(y1+y2)/2+ly,lbl,ha='center',va='center',fontsize=fs,
                bbox=dict(fc='white',ec='none',pad=.1),zorder=6)

def seg(ax,x1,y1,x2,y2,lw=1.3,color='black'):
    ax.plot([x1,x2],[y1,y2],color=color,lw=lw,zorder=4)

def dot(ax,x,y):
    ax.plot(x,y,'o',color='black',ms=4,zorder=6)


# ═══════════════════════════════════════════════════════════
# 1. DFD «Просмотр трансляции» (BPWin DFD, Yourdon)
# ═══════════════════════════════════════════════════════════
def gen_dfd():
    W,H=20,14
    fig,ax=new_fig(W,H)
    ct,cb=bpwin_frame(ax,W,H,
                      node='A0',
                      title='DFD — Просмотр трансляции',
                      context='A0',
                      number='001')

    # content region: x=[0..W], y=[cb..ct]
    # Внешние сущности
    ext_entity(ax, 1.6, 10.2, 2.4, 1.0, 1, 'Зритель')
    ext_entity(ax, 1.6,  5.2, 2.4, 1.0, 2, 'Smart TV\nустройство')
    ext_entity(ax, 1.6,  7.7, 2.4, 1.0, 3, 'Сервер\nтрансляций')

    # Хранилища
    data_store(ax, 12.0, 11.4, 4.0, 0.70, 1, 'Каталог трансляций')
    data_store(ax,  6.5,  3.2, 4.0, 0.70, 2, 'Кэш каталога')
    data_store(ax, 14.5,  3.2, 4.0, 0.70, 3, 'История сеансов')

    # Процессы
    process_box(ax,  7.0, 10.2, 3.5, 1.4, 1, 'Загрузка\nкаталога')
    process_box(ax,  7.0,  7.0, 3.5, 1.4, 2, 'Формирование\nUI каталога')
    process_box(ax, 11.5, 10.2, 3.5, 1.4, 3, 'Инициализация\nсеанса')
    process_box(ax, 11.5,  7.0, 3.5, 1.4, 4, 'Управление\nвоспроизведением')
    process_box(ax, 16.5,  7.0, 3.0, 1.4, 5, 'Обработка\nошибок')
    process_box(ax, 16.5, 10.2, 3.0, 1.4, 6, 'Регистрация\nсеанса')

    # ── Потоки ───────────────────────────────────────────────
    # Зритель → процессы
    fl(ax, 2.8, 10.4, 5.25,10.5, 'Запрос каталога')
    fl(ax, 2.8, 10.0, 9.75,10.0, 'Выбор трансляции\n(matchId)', rad=-0.15, ly=0.22)
    fl(ax, 2.8,  9.8, 5.25, 7.3, 'Команды\nнавигации', rad=0.15, lx=-0.5, ly=0)
    fl(ax, 2.8,  9.6, 9.75, 7.3, 'Команды плеера\n(пауза/выход)', rad=0.20, lx=0.3, ly=-0.15)

    # P1 ↔ Сервер
    fl(ax, 2.8, 7.9, 5.25,10.1, 'HTTP-запрос (SportTV API)', rad=0.12, lx=-0.4, ly=0.2)
    fl(ax, 5.25,10.3, 2.8, 8.1, 'JSON список матчей',       rad=0.12, lx= 0.4, ly=-0.2)

    # P3 ↔ Сервер
    fl(ax, 2.8, 7.6, 9.75,10.3,'Запрос URL потока', rad=-0.12, ly=0.2)
    fl(ax, 9.75,9.9, 2.8, 7.4, 'URL потока (HLS/DASH)', rad=-0.12, ly=-0.22)

    # P1 → D1 (Каталог)
    fl(ax, 7.0,10.9, 10.0,11.4,'Сохранить данные\nкаталога', ly=0.22)
    # D1 → P1
    fl(ax,10.0,11.05, 7.0,10.9,'Данные каталога', ly=-0.25, rad=0.15)

    # P1 → P2
    fl(ax, 7.0, 9.5, 7.0, 7.7,'Данные каталога')

    # P1 → D2 (кэш)
    seg(ax,6.5,9.5,6.5,3.55)
    dot(ax,6.5,9.5)
    ax.annotate('',xy=(6.5,3.55),xytext=(6.5,9.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=1.3))
    ax.text(5.8,6.5,'Кэшировать',ha='center',fontsize=8,rotation=90,
            bbox=dict(fc='white',ec='none'),zorder=5)

    # D2 → P2
    fl(ax, 6.5, 3.55, 7.0, 6.3,'Данные из кэша')

    # P2 → Smart TV (экран)
    fl(ax, 5.25, 7.0, 2.8, 5.5,'Экран каталога\n(Compose UI)', lx=-0.2, ly=0)

    # P3 → P4
    fl(ax,11.5, 9.5,11.5, 7.7,'Параметры потока\n(URL, title, isLive)')

    # P3 → P6
    fl(ax,13.25,10.2,15.0,10.2,'Данные сеанса\n(matchId, userId, ts)')

    # P6 → D3
    fl(ax,16.5, 9.5,16.5, 3.55,'Запись сеанса',lx=0.7)

    # D3 → P6
    seg(ax,17.0,3.55,17.0,9.5)
    dot(ax,17.0,3.55)
    ax.annotate('',xy=(17.0,9.5),xytext=(17.0,3.55),
                arrowprops=dict(arrowstyle='->',color='black',lw=1.3))
    ax.text(17.7,6.5,'Подтверждение\nзаписи',ha='center',fontsize=8,rotation=90,
            bbox=dict(fc='white',ec='none'),zorder=5)

    # P4 → Smart TV
    fl(ax,9.75, 7.0, 2.8, 5.0,'Видеопоток на экране', rad=0.15, lx=-0.3, ly=-0.15)

    # P4 → P5
    fl(ax,13.25, 7.0,15.0, 7.0,'Событие ошибки (код, тип)')

    # P5 → P4
    fl(ax,15.0, 6.7,13.25,6.7,'Команда повтора/отмены', rad=-0.15, ly=-0.25)

    # P5 → Smart TV (уведомление)
    fl(ax,15.0, 7.3, 2.8, 5.5,'Уведомление об ошибке', rad=0.18, lx=-1.0, ly=0.2)

    save(fig,'bpwin_01_dfd.png')


# ═══════════════════════════════════════════════════════════
# 2. IDEF0 Context A-0  (BPWin IDEF0)
# ═══════════════════════════════════════════════════════════
def gen_idef0_ctx():
    W,H=20,14
    fig,ax=new_fig(W,H)
    ct,cb=bpwin_frame(ax,W,H,
                      node='A-0',
                      title='Контекстная диаграмма IDEF0',
                      context='A-0',
                      number='002')

    BX,BY,BW,BH=6.5,4.5,7.0,4.0
    idef_box(ax, BX+BW/2, BY+BH/2, BW, BH, 'A0',
             ['A-0','','Обеспечить просмотр','спортивных трансляций','на Smart TV устройстве'])

    LW=1.5

    # ── ВХОДЫ (слева) ──────────────────────────────────────
    # I1
    seg(ax,0.5,7.0,BX,7.0,lw=LW)
    ax.annotate('',xy=(BX,7.0),xytext=(0.5,7.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(0.55,7.22,'I1: Запрос пользователя\n(навигация, выбор трансляции)',
            ha='left',va='bottom',fontsize=9.5)

    # I2
    seg(ax,0.5,5.5,BX,5.5,lw=LW)
    ax.annotate('',xy=(BX,5.5),xytext=(0.5,5.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(0.55,5.72,'I2: Команды управления плеером\n(пауза, перемотка, выход)',
            ha='left',va='bottom',fontsize=9.5)

    # ── ВЫХОДЫ (справа) ────────────────────────────────────
    outs=[(8.5,'O1: Видеопоток на экране (HLS/DASH)'),
          (7.0,'O2: UI каталога и навигации'),
          (5.5,'O3: Уведомления об ошибках и статусах')]
    for y,lbl in outs:
        seg(ax,BX+BW,y,19.0,y,lw=LW)
        ax.annotate('',xy=(19.0,y),xytext=(BX+BW,y),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        ax.text(19.1,y,lbl,ha='left',va='center',fontsize=9.5)

    # ── УПРАВЛЕНИЕ (сверху) ─────────────────────────────────
    ctrl=[(8.0, 'C1: Требования к качеству\nвидео (битрейт, разрешение)'),
          (10.0,'C2: Правила доступа\nк трансляциям'),
          (12.0,'C3: Протоколы\nHLS / DASH')]
    for x,lbl in ctrl:
        seg(ax,x,BY+BH,x,11.5,lw=LW)
        ax.annotate('',xy=(x,BY+BH),xytext=(x,11.5),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        ax.text(x,11.6,lbl,ha='center',va='bottom',fontsize=9.5)

    seg(ax,8.0,11.5,12.0,11.5,lw=LW)
    for x,_ in ctrl: dot(ax,x,11.5)

    # ── МЕХАНИЗМЫ (снизу) ─── стрелки снизу вверх ──────────
    mechs=[(8.0, 'M1: Android TV OS\n+ ExoPlayer'),
           (10.0,'M2: Сервер трансляций\n(SportTV API)'),
           (12.0,'M3: Smart TV устройство\n(аппаратная часть)')]
    for x,lbl in mechs:
        seg(ax,x,BY,x,3.0,color='#444',lw=LW)
        ax.annotate('',xy=(x,BY),xytext=(x,3.0),
                    arrowprops=dict(arrowstyle='->',color='#444',lw=LW))
        ax.text(x,2.85,lbl,ha='center',va='top',fontsize=9.5,color='#333')

    seg(ax,8.0,3.0,12.0,3.0,lw=1.2)
    for x,_ in mechs: dot(ax,x,3.0)

    save(fig,'bpwin_02_idef0_ctx.png')


# ═══════════════════════════════════════════════════════════
# 3. IDEF0 Decomp A0  (BPWin IDEF0)
# ═══════════════════════════════════════════════════════════
def gen_idef0_decomp():
    W,H=20,14
    fig,ax=new_fig(W,H)
    ct,cb=bpwin_frame(ax,W,H,
                      node='A0',
                      title='Декомпозиция IDEF0',
                      context='A0',
                      number='003')

    BW,BH=3.8,2.2
    LW=1.4

    # Функциональные блоки
    idef_box(ax,  4.0,10.0,BW,BH,'A1',['Аутентификация','пользователя'])
    idef_box(ax, 10.5,10.0,BW,BH,'A2',['Загрузка каталога','трансляций'])
    idef_box(ax,  4.0, 6.5,BW,BH,'A3',['Просмотр','трансляции'])
    idef_box(ax, 10.5, 6.5,BW,BH,'A4',['Регистрация','сеанса просмотра'])
    idef_box(ax, 16.5, 8.25,BW,BH,'A5',['Обработка','ошибок'])

    # ── Входы (левая граница) ──────────────────────────────
    seg(ax,0.4,10.4,2.1,10.4,lw=LW)
    ax.annotate('',xy=(2.1,10.4),xytext=(0.4,10.4),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(0.5,10.62,'I1: Данные аутентификации\n(email / код)',ha='left',va='bottom',fontsize=8.5)

    seg(ax,0.4, 6.9,2.1, 6.9,lw=LW)
    ax.annotate('',xy=(2.1,6.9),xytext=(0.4,6.9),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(0.5,7.12,'I2: Команды управления\nплеером',ha='left',va='bottom',fontsize=8.5)

    # ── Внутренние потоки ──────────────────────────────────
    # A1 → A2 (токен)
    seg(ax,5.9,10.0,8.6,10.0,lw=LW)
    ax.annotate('',xy=(8.6,10.0),xytext=(5.9,10.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(7.25,10.2,'Токен доступа',ha='center',va='bottom',fontsize=8.5)

    # A1 → A3 (разрешение) — вертикально
    seg(ax,4.0,8.9,4.0,7.6,lw=LW)
    ax.annotate('',xy=(4.0,7.6),xytext=(4.0,8.9),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(4.6,8.25,'Разрешение\nна просмотр',ha='left',va='center',fontsize=8.5)

    # A2 → A3 (URL потока) — L-образно
    seg(ax,10.5,9.0,10.5,8.0,lw=LW)
    seg(ax,10.5,8.0, 5.9,8.0,lw=LW)
    ax.annotate('',xy=(5.9,7.5),xytext=(5.9,8.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    dot(ax,10.5,8.0)
    ax.text(8.3,8.2,'URL потока (HLS/DASH)',ha='center',va='bottom',fontsize=8.5)

    # A2 → A4 (выбранная трансляция)
    seg(ax,10.5,8.9,10.5,7.6,lw=LW)
    ax.annotate('',xy=(10.5,7.6),xytext=(10.5,8.9),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(11.2,8.25,'Выбранная\nтрансляция',ha='left',va='center',fontsize=8.5)

    # A3 → A4 (данные сеанса)
    seg(ax,5.9, 6.5,8.6, 6.5,lw=LW)
    ax.annotate('',xy=(8.6,6.5),xytext=(5.9,6.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(7.25,6.72,'Данные сеанса\n(matchId, userId, ts)',ha='center',va='bottom',fontsize=8.5)

    # A3 → A5 (ошибки) — через низ
    seg(ax, 5.9, 6.0, 5.9, 4.5,lw=LW)
    seg(ax, 5.9, 4.5,16.5, 4.5,lw=LW)
    ax.annotate('',xy=(16.5,7.25),xytext=(16.5,4.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(11.5,4.7,'Ошибки воспроизведения',ha='center',va='bottom',fontsize=8.5)

    # A5 → A3 (повтор) — через верх
    seg(ax,16.5, 9.35,16.5,11.5,lw=LW)
    seg(ax,16.5,11.5, 4.0,11.5,lw=LW)
    seg(ax, 4.0,11.5, 4.0,11.1,lw=LW)
    ax.annotate('',xy=(4.0,11.1),xytext=(4.0,11.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(10.5,11.72,'Команда повтора / отмены',ha='center',va='bottom',fontsize=8.5)

    # ── Выходы (правая граница) ────────────────────────────
    for x,y,lbl in [(12.4,10.4,'O1: Каталог трансляций на экране'),
                    ( 5.9, 6.0,'O2: Видеопоток на экране'),
                    (12.4, 6.2,'O3: История просмотра'),
                    (18.4, 8.25,'O4: Уведомления об ошибках')]:
        ax.annotate('',xy=(19.6,y),xytext=(x,y),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        ax.text(19.7,y,lbl,ha='left',va='center',fontsize=8.5)

    # ── Управление (сверху) ─────────────────────────────────
    for x,lbl in [(4.0,'C1: Правила\nаутентификации'),
                  (10.5,'C2: Правила доступа\nк контенту')]:
        seg(ax,x,11.1,x,12.5,lw=LW)
        ax.annotate('',xy=(x,11.1),xytext=(x,12.5),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        ax.text(x,12.65,lbl,ha='center',va='bottom',fontsize=8.5)

    # ── Механизмы (снизу) ──────────────────────────────────
    for x,lbl in [(4.0,'M1: Android TV OS\n+ ExoPlayer'),
                  (10.5,'M2: SportTV API'),
                  (16.5,'M3: Smart TV устройство')]:
        ax.annotate('',xy=(x,5.5),xytext=(x,4.0),
                    arrowprops=dict(arrowstyle='->',color='#444',lw=1.2))
        ax.text(x,3.85,lbl,ha='center',va='top',fontsize=8.5,color='#333')

    save(fig,'bpwin_03_idef0_decomp.png')


# ═══════════════════════════════════════════════════════════
# 4. USE CASE (UML, BPWin рамка)
# ═══════════════════════════════════════════════════════════
def gen_usecase():
    from matplotlib.patches import Ellipse
    W,H=20,14
    fig,ax=new_fig(W,H)
    ct,cb=bpwin_frame(ax,W,H,
                      node='UC',
                      title='Диаграмма вариантов использования',
                      context='UC',
                      number='004')

    def actor(cx,cy,name):
        ax.add_patch(plt.Circle((cx,cy+0.95),.28,fc='white',ec='black',lw=1.4,zorder=4))
        ax.plot([cx,cx],[cy+.67,cy+.08],'k-',lw=1.3,zorder=4)
        ax.plot([cx-.38,cx+.38],[cy+.38,cy+.38],'k-',lw=1.3,zorder=4)
        ax.plot([cx,cx-.34],[cy+.08,cy-.42],'k-',lw=1.3,zorder=4)
        ax.plot([cx,cx+.34],[cy+.08,cy-.42],'k-',lw=1.3,zorder=4)
        for i,ln in enumerate(name.split('\n')):
            ax.text(cx,cy-.62-i*.24,ln,ha='center',va='top',fontsize=9,fontweight='bold')

    def uc(cx,cy,w,h,txt):
        ax.add_patch(Ellipse((cx,cy),w,h,fc='white',ec='black',lw=1.5,zorder=3))
        for i,ln in enumerate(txt.split('\n')):
            off=(len(txt.split('\n'))-1)/2*.22-i*.24
            ax.text(cx,cy+off,ln,ha='center',va='center',fontsize=9,zorder=4)

    def assoc(x1,y1,x2,y2):
        ax.plot([x1,x2],[y1,y2],'k-',lw=.9,zorder=2)

    def dep(x1,y1,x2,y2,lbl):
        ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->',color='black',lw=1.0,
                                   linestyle='dashed'))
        ax.text((x1+x2)/2+.05,(y1+y2)/2+.14,lbl,ha='center',fontsize=8,
                style='italic',bbox=dict(fc='white',ec='none',pad=.08),zorder=5)

    # Граница системы
    ax.add_patch(FancyBboxPatch((3.0,1.4),13.5,11.3,boxstyle='square,pad=0',
                                fc='none',ec='black',lw=1.8,zorder=1))
    ax.text(9.75,12.9,'Приложение «СвойСпорт TV»',ha='center',fontsize=10,style='italic')

    # Акторы
    actor(1.3, 7.2, 'Зритель')
    actor(18.2,10.2,'Адми-\nнистратор')

    # ВИ Зрителя
    viewer=[
        (9.0,12.2,4.4,.70,'Запустить приложение'),
        (9.0,11.0,4.6,.70,'Аутентифицироваться\n(ввести код)'),
        (9.0, 9.8,4.4,.70,'Просмотреть главный экран'),
        (9.0, 8.6,4.6,.70,'Получить список трансляций'),
        (9.0, 7.4,4.4,.70,'Просмотреть каталог'),
        (9.0, 6.2,4.4,.70,'Выбрать трансляцию'),
        (9.0, 5.0,4.6,.70,'Просмотреть детали матча'),
        (9.0, 3.8,4.6,.70,'Запустить воспроизведение'),
        (9.0, 2.6,4.8,.70,'Управлять воспроизведением'),
    ]
    for cx,cy,w,h,txt in viewer:
        uc(cx,cy,w,h,txt)
        assoc(1.65,7.65,cx-w/2,cy)

    # include / extend
    uc(14.5,3.8,3.8,.70,'«include»\nЗагрузить HLS-поток')
    uc(14.5,2.6,3.8,.70,'«extend»\nПоставить на паузу')
    dep(11.2,3.8,12.55,3.8,'«include»')
    dep(11.2,2.6,12.55,2.6,'«extend»')

    # ВИ Администратора
    admin=[
        (13.5,12.2,4.8,.70,'Настроить подключение\nк серверу трансляций'),
        (13.5,11.0,4.8,.70,'Управлять каталогом\nтрансляций'),
        (13.5, 9.8,4.8,.70,'Провести тестовый\nзапуск трансляции'),
    ]
    for cx,cy,w,h,txt in admin:
        uc(cx,cy,w,h,txt)
        assoc(17.8,10.65,cx+w/2,cy)

    save(fig,'bpwin_04_usecase.png')


# ═══════════════════════════════════════════════════════════
# 5. STATE DIAGRAM (UML, BPWin рамка)
# ═══════════════════════════════════════════════════════════
def gen_state():
    W,H=20,14
    fig,ax=new_fig(W,H)
    ct,cb=bpwin_frame(ax,W,H,
                      node='ST',
                      title='Диаграмма состояний сеанса просмотра',
                      context='ST',
                      number='005')

    def st(cx,cy,w,h,name,action=''):
        shadow(ax,cx-w/2,cy-h/2,w,h,r=0.06,lw=1.5)
        if action:
            ax.plot([cx-w/2,cx+w/2],[cy+h/2-.40,cy+h/2-.40],'k-',lw=1.0,zorder=4)
            ax.text(cx,cy+h/2-.20,name,ha='center',va='center',
                    fontsize=10,fontweight='bold',zorder=5)
            ax.text(cx,cy-.07,action,ha='center',va='center',
                    fontsize=8,color='#333',style='italic',zorder=5)
        else:
            ax.text(cx,cy,name,ha='center',va='center',
                    fontsize=10,fontweight='bold',zorder=5)

    def tr(x1,y1,x2,y2,lbl='',rad=0,lx=0,ly=.18,fs=8.5):
        ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->',color='black',lw=1.3,
                                   connectionstyle=f'arc3,rad={rad}'))
        if lbl:
            ax.text((x1+x2)/2+lx,(y1+y2)/2+ly,lbl,ha='center',va='center',fontsize=fs,
                    bbox=dict(fc='white',ec='none',pad=.1),zorder=6)

    BW,BH=4.8,1.0

    ax.add_patch(plt.Circle((10,12.8),.22,fc='black',ec='black',zorder=5))
    st(10,11.8, BW,BH,'Инициализация','entry / запуск PlayerManager')
    st(10,10.4, BW,BH,'Загрузка параметров','do / HTTP-запрос URL потока')
    st(10, 9.0, BW,BH,'Буферизация','do / ExoPlayer.prepare()')
    st( 6, 7.5, BW,BH,'Воспроизведение','do / ExoPlayer.play()')
    st( 6, 6.0, BW,BH,'Пауза','do / ExoPlayer.pause()')
    st(14, 7.5, 4.8,BH,'Ошибка','entry / показать сообщение об ошибке')
    st(14, 6.0, 4.8,BH,'Повтор подключения','do / retry  (макс. 3 попытки)')
    st(10, 4.2, BW,BH,'Завершён','exit / PlayerManager.release()')
    ax.add_patch(plt.Circle((10,3.15),.28,fc='white',ec='black',lw=2,zorder=4))
    ax.add_patch(plt.Circle((10,3.15),.18,fc='black',zorder=5))

    tr(10,12.56,10,12.30,'Запуск плеера')
    tr(10,11.30,10,10.90,'matchId получен')
    tr(10, 9.90,10, 9.50,'URL потока получен')
    tr( 8.6,8.55, 7.0,8.0,'Буфер заполнен')
    tr( 6,7.0, 6,6.5,'Команда «Пауза»',rad=.28,lx=-1.7)
    tr( 6,6.5, 6,7.0,'Команда «Продолжить»',rad=.28,lx=1.7)
    tr( 8.4,7.5,11.6,7.5,'Потеря соединения')
    tr(11.6,9.0,14,8.0,'Таймаут / сервер недоступен',lx=1.5,ly=0)
    tr(14,7.0,14,6.5,'Повторная попытка',rad=.28,lx=2.0)
    tr(11.6,6.0,10,8.55,'Соединение восстановлено',rad=-.25,lx=-1.8,ly=.2)
    tr( 6,5.5, 8.6,4.7,'Команда «Выход»')
    tr( 7.0,7.0, 8.6,4.7,'Команда «Выход»',rad=-.10,lx=-1.5,ly=0)
    tr(11.6,7.5,11.4,4.7,'Отмена',rad=.12,lx=.7,ly=0)
    tr(11.6,6.0,11.4,4.7,'Превышен лимит попыток',lx=2.0,ly=0)
    tr(10, 3.70,10, 3.43,'')

    # примечание
    ax.text(1.0,7.8,
            'Внутренние действия:\n'
            'do / обновление прогресс-бара\n'
            'do / контроль буфера\n'
            'do / heartbeat к серверу',
            fontsize=8,bbox=dict(boxstyle='round',fc='#FFFFF0',ec='#999',pad=.3))

    save(fig,'bpwin_05_state.png')


# ════════════════════════════════════════════════════════════
if __name__=='__main__':
    import matplotlib.pyplot as plt
    gen_dfd()
    gen_idef0_ctx()
    gen_idef0_decomp()
    gen_usecase()
    gen_state()
    print(f'\nВсе файлы сохранены в: {OUT}')
