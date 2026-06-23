"""
PNG-превью в стиле BPWin/Visio (ч/б, чистые)
"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, Ellipse
import numpy as np, os

OUT = os.path.dirname(os.path.abspath(__file__))
BK, WH = 'black', 'white'

def save(fig, name):
    plt.savefig(f'{OUT}/{name}', dpi=200, bbox_inches='tight',
                facecolor='white', edgecolor='none')
    plt.close(fig)
    print(f'✓ {name}')

# ─── arrow helper ───────────────────────────────────────────
def arr(ax, x1,y1,x2,y2, lbl='', rad=0.0, lx=0,ly=.12,
        ls='-', color='black', lw=1.3, fs=8):
    ax.annotate('', xy=(x2,y2), xytext=(x1,y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                linestyle=ls,
                                connectionstyle=f'arc3,rad={rad}'))
    if lbl:
        mx,my=(x1+x2)/2+lx,(y1+y2)/2+ly
        ax.text(mx,my,lbl,ha='center',va='center',fontsize=fs,
                bbox=dict(fc='white',ec='none',pad=.1),zorder=6)

def seg(ax,x1,y1,x2,y2,color='black',lw=1.3,ls='-'):
    ax.plot([x1,x2],[y1,y2],color=color,lw=lw,ls=ls,zorder=4)

def dot(ax,x,y):
    ax.plot(x,y,'o',color='black',ms=4,zorder=6)

def lbl(ax,x,y,txt,ha='center',fs=9,bold=False):
    ax.text(x,y,txt,ha=ha,va='center',fontsize=fs,
            fontweight='bold' if bold else 'normal',
            bbox=dict(fc='white',ec='none',pad=.1),zorder=5)

# ════════════════════════════════════════════════════════════
# 1. USE CASE  (Visio/UML style, B&W)
# ════════════════════════════════════════════════════════════
def preview_usecase():
    fig,ax=plt.subplots(figsize=(14,18))
    ax.set_xlim(0,14); ax.set_ylim(0,18); ax.axis('off')
    fig.patch.set_facecolor('white')

    def actor(x,y,name):
        ax.add_patch(plt.Circle((x,y+1.05),.30,fc='white',ec='black',lw=1.5,zorder=4))
        ax.plot([x,x],[y+.75,y+.1],'k-',lw=1.4,zorder=4)
        ax.plot([x-.40,x+.40],[y+.43,y+.43],'k-',lw=1.4,zorder=4)
        ax.plot([x,x-.36],[y+.1,y-.48],'k-',lw=1.4,zorder=4)
        ax.plot([x,x+.36],[y+.1,y-.48],'k-',lw=1.4,zorder=4)
        for i,ln in enumerate(name.split('\n')):
            ax.text(x,y-.65-i*.25,ln,ha='center',va='top',fontsize=9,fontweight='bold')

    def uc(cx,cy,w,h,txt):
        ax.add_patch(Ellipse((cx,cy),w,h,fc='white',ec='black',lw=1.5,zorder=3))
        for i,ln in enumerate(txt.split('\n')):
            off=(len(txt.split('\n'))-1)/2*.21-i*.23
            ax.text(cx,cy+off,ln,ha='center',va='center',fontsize=8.5,zorder=4)

    def assoc(x1,y1,x2,y2):
        ax.plot([x1,x2],[y1,y2],'k-',lw=.9,zorder=2)

    # Title
    ax.text(7,17.7,'Диаграмма вариантов использования',ha='center',fontsize=13,fontweight='bold')
    ax.text(7,17.25,'Система «СвойСпорт TV»',ha='center',fontsize=10,color='#444')

    # Boundary
    ax.add_patch(FancyBboxPatch((2.0,.8),10,16.0,boxstyle='square,pad=0',
                                fc='none',ec='black',lw=1.8,zorder=1))
    ax.text(7,17.0,'Приложение «СвойСпорт TV»',ha='center',fontsize=9,
            color='black',style='italic')

    # Зритель
    actor(.9,8.5,'Зритель')

    vc=[
        (6.0,16.0,3.8,.70,'Запустить приложение'),
        (6.0,14.9,3.8,.70,'Аутентифицироваться\n(ввести код)'),
        (6.0,13.7,3.8,.70,'Просмотреть главный экран'),
        (6.0,12.5,3.8,.70,'Получить список трансляций'),
        (6.0,11.3,3.8,.70,'Просмотреть каталог'),
        (6.0,10.1,3.8,.70,'Выбрать трансляцию'),
        (6.0, 8.9,3.8,.70,'Просмотреть детали матча'),
        (6.0, 7.7,3.8,.70,'Запустить воспроизведение'),
        (6.0, 6.5,3.8,.70,'Управлять воспроизведением'),
        (6.0, 5.3,3.8,.70,'Просмотреть расписание'),
        (6.0, 4.1,4.0,.70,'Получить уведомление\nоб ошибке'),
    ]
    for cx,cy,w,h,txt in vc:
        uc(cx,cy,w,h,txt)
        assoc(1.42,9.0,cx-w/2,cy)

    # include / extend targets
    uc(9.5,7.7,3.4,.70,'«include»\nЗагрузить HLS-поток')
    uc(9.5,6.5,3.4,.70,'«extend»\nПоставить на паузу')
    ax.annotate('',xy=(7.75,7.7),xytext=(6.0+1.9,7.7),
                arrowprops=dict(arrowstyle='->',lw=1,linestyle='dashed',color='black'))
    ax.text(8.35,7.92,'«include»',ha='center',fontsize=7.5,style='italic')
    ax.annotate('',xy=(7.75,6.5),xytext=(6.0+1.9,6.5),
                arrowprops=dict(arrowstyle='->',lw=1,linestyle='dashed',color='black'))
    ax.text(8.35,6.72,'«extend»',ha='center',fontsize=7.5,style='italic')

    # Admin
    actor(13.1,12.5,'Адми-\nнистратор')
    ac=[
        (9.8,16.0,3.6,.70,'Настроить подключение\nк серверу трансляций'),
        (9.8,14.9,3.6,.70,'Управлять каталогом\nтрансляций'),
        (9.8,13.7,3.6,.70,'Провести тестовый\nзапуск трансляции'),
    ]
    for cx,cy,w,h,txt in ac:
        uc(cx,cy,w,h,txt)
        assoc(13.1,13.0,cx+w/2,cy)

    save(fig,'preview_01_use_case.png')


# ════════════════════════════════════════════════════════════
# 2. STATE DIAGRAM  (UML, B&W)
# ════════════════════════════════════════════════════════════
def preview_state():
    fig,ax=plt.subplots(figsize=(13,18))
    ax.set_xlim(0,13); ax.set_ylim(0,18); ax.axis('off')
    fig.patch.set_facecolor('white')

    def st(cx,cy,w,h,name,action=''):
        ax.add_patch(FancyBboxPatch((cx-w/2,cy-h/2),w,h,
                                   boxstyle='round,pad=.1',
                                   fc='white',ec='black',lw=1.6,zorder=3))
        if action:
            ax.plot([cx-w/2,cx+w/2],[cy+h/2-.38,cy+h/2-.38],'k-',lw=1.0,zorder=4)
            ax.text(cx,cy+h/2-.19,name,ha='center',va='center',
                    fontsize=9.5,fontweight='bold',zorder=5)
            ax.text(cx,cy-.06,action,ha='center',va='center',
                    fontsize=7.5,color='#333',style='italic',zorder=5)
        else:
            ax.text(cx,cy,name,ha='center',va='center',
                    fontsize=9.5,fontweight='bold',zorder=5)

    W,H=4.0,.80
    ax.text(6.5,17.7,'Диаграмма состояний сеанса просмотра трансляции',
            ha='center',fontsize=12,fontweight='bold')

    st(6.5,16.4,W,H,'Инициализация','entry / запуск PlayerManager')
    st(6.5,14.8,W,H,'Загрузка параметров','do / HTTP-запрос URL потока')
    st(6.5,13.1,W,H,'Буферизация','do / ExoPlayer.prepare()')
    st(3.8,11.0,W,H,'Воспроизведение','do / ExoPlayer.play()')
    st(3.8, 8.9,W,H,'Пауза','do / ExoPlayer.pause()')
    st(9.5,11.0,4.0,H,'Ошибка','entry / показать сообщение')
    st(9.5, 8.9,4.0,H,'Повтор подключения','do / retry  (макс. 3 попытки)')
    st(6.5, 6.6,W,H,'Завершён','exit / PlayerManager.release()')

    # init / final
    ax.add_patch(plt.Circle((6.5,17.2),.20,fc='black',ec='black',zorder=5))
    ax.add_patch(plt.Circle((6.5,5.5),.28,fc='white',ec='black',lw=1.8,zorder=4))
    ax.add_patch(plt.Circle((6.5,5.5),.18,fc='black',zorder=5))

    def tr(x1,y1,x2,y2,lbl='',rad=0,lx=0,ly=.14,ls='-'):
        ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->',color='black',lw=1.2,
                                   linestyle=ls,
                                   connectionstyle=f'arc3,rad={rad}'))
        if lbl:
            ax.text((x1+x2)/2+lx,(y1+y2)/2+ly,lbl,ha='center',va='center',
                    fontsize=7.8,bbox=dict(fc='white',ec='none',pad=.1),zorder=6)

    tr(6.5,17.0,6.5,16.8,'Запуск плеера')
    tr(6.5,16.0,6.5,15.2,'matchId получен')
    tr(6.5,14.4,6.5,13.5,'URL потока получен')
    tr(5.5,12.74,4.3,11.4,'Буфер заполнен')
    tr(3.8,10.6,3.8,9.3,'Команда «Пауза»',rad=.30,lx=-1.55)
    tr(3.8,9.3,3.8,10.6,'Команда «Продолжить»',rad=.30,lx=1.55)
    tr(5.8,11.0,7.5,11.0,'Потеря соединения')
    tr(7.8,13.1,9.5,11.4,'Таймаут / сервер\nнедоступен',lx=1.2,ly=0)
    tr(9.5,10.6,9.5,9.3,'Повторная попытка',rad=.30,lx=1.8)
    tr(8.3,9.0,7.0,13.0,'Соединение\nвосстановлено',rad=-.25,lx=-1.6,ly=.2)
    tr(3.8,8.5,5.5,7.0,'Команда «Выход»')
    tr(4.4,10.6,5.5,7.0,'Команда «Выход»',rad=-.12,lx=-1.5,ly=0)
    tr(9.5,10.6,7.5,7.0,'Отмена',rad=.12,lx=.8,ly=0)
    tr(9.5,8.5,7.5,7.0,'Превышен лимит попыток',lx=1.5,ly=0)
    tr(6.5,6.2,6.5,5.78,'')

    save(fig,'preview_02_state.png')


# ════════════════════════════════════════════════════════════
# 3. IDEF0 CONTEXT A-0  (BPWin style, B&W)
# ════════════════════════════════════════════════════════════
def preview_idef0_ctx():
    fig,ax=plt.subplots(figsize=(18,12))
    ax.set_xlim(0,18); ax.set_ylim(0,12); ax.axis('off')
    fig.patch.set_facecolor('white')

    # outer frame
    ax.add_patch(FancyBboxPatch((.1,.8),17.8,10.8,boxstyle='square,pad=0',
                                fc='none',ec='black',lw=2,zorder=0))
    ax.text(9,11.35,'Диаграмма IDEF0 — Контекстная диаграмма (A-0)',
            ha='center',fontsize=13,fontweight='bold')
    ax.text(9,10.88,'Система: «СвойСпорт TV»  Назначение: просмотр '
                    'спортивных трансляций на Smart TV  Дата: 2026-05-24',
            ha='center',fontsize=8.8,color='#333')

    # main box
    BX,BY,BW,BH=5.8,3.2,6.4,3.6
    ax.add_patch(FancyBboxPatch((BX,BY),BW,BH,boxstyle='square,pad=0',
                                fc='white',ec='black',lw=2,zorder=3))
    box_lines=['A-0','','Обеспечить просмотр','спортивных трансляций','на Smart TV устройстве']
    for i,ln in enumerate(box_lines):
        off=(len(box_lines)-1)/2*.42-i*.42
        fw='bold' if i==0 else 'normal'
        fs=10 if i==0 else 11 if i>1 else 9
        ax.text(BX+BW/2,BY+BH/2+off,ln,ha='center',va='center',
                fontsize=fs,fontweight=fw,zorder=4)

    A='black'; LW=1.5

    # ── INPUTS ──────────────────────────────────────────────
    seg(ax,.3,5.0,BX,5.0,lw=LW); arr(ax,BX-.01,5.0,BX,5.0,lw=LW)
    seg(ax,.3,4.0,BX,4.0,lw=LW); arr(ax,BX-.01,4.0,BX,4.0,lw=LW)
    seg(ax,.3,4.0,.3,5.0,lw=LW); dot(ax,.3,4.0); dot(ax,.3,5.0)
    ax.text(-.05,4.5,'ВХОД',ha='right',va='center',fontsize=8,fontweight='bold',rotation=90)
    ax.text(.9,5.2,'I1: Запрос пользователя\n(навигация, выбор трансляции)',
            ha='left',va='bottom',fontsize=8.5)
    ax.text(.9,4.2,'I2: Команды управления\nплеером (пауза, выход)',
            ha='left',va='bottom',fontsize=8.5)

    # ── OUTPUTS ─────────────────────────────────────────────
    for y,lbl2 in [(5.5,'O1: Видеопоток на экране (HLS/DASH)'),
                   (5.0,'O2: UI каталога и навигации'),
                   (4.5,'O3: Уведомления об ошибках')]:
        seg(ax,BX+BW,y,16.5,y,lw=LW)
        ax.text(16.6,y,lbl2,ha='left',va='center',fontsize=8.5)
    seg(ax,16.5,4.5,16.5,5.5,lw=LW); dot(ax,16.5,4.5); dot(ax,16.5,5.0); dot(ax,16.5,5.5)
    arr(ax,16.5,5.0,17.5,5.0,lw=LW)
    ax.text(17.6,5.0,'ВЫХОД',ha='left',va='center',fontsize=8,fontweight='bold')

    # ── CONTROLS ────────────────────────────────────────────
    ctrls=[(7.0,'C1: Требования к качеству\nвидео'),(9.0,'C2: Правила доступа\nк трансляциям'),
           (11.2,'C3: Протоколы\nHLS / DASH')]
    for x,lbl2 in ctrls:
        seg(ax,x,BY,x,10.0,lw=LW); arr(ax,x,10.01,x,BY,lw=LW)
        ax.text(x,10.15,lbl2,ha='center',va='bottom',fontsize=8.5)
    for x,_ in ctrls: dot(ax,x,10.0)
    seg(ax,7.0,10.0,11.2,10.0,lw=LW)
    seg(ax,9.0,10.0,9.0,10.6,lw=LW)
    ax.text(9.0,10.75,'УПРАВЛЕНИЕ',ha='center',fontsize=8,fontweight='bold')

    # ── MECHANISMS ──────────────────────────────────────────
    mechs=[(7.0,'M1: Android TV OS\n+ ExoPlayer'),
           (9.0,'M2: Сервер трансляций\n(SportTV API)'),
           (11.0,'M3: Smart TV устройство')]
    for x,lbl2 in mechs:
        seg(ax,x,BY,x,2.4,color='#555',lw=LW)
        ax.annotate('',xy=(x,BY),xytext=(x,2.4),
                    arrowprops=dict(arrowstyle='->',color='#555',lw=LW))
        ax.text(x,2.25,lbl2,ha='center',va='top',fontsize=8.5,color='#333')
    for x,_ in mechs: dot(ax,x,2.4)
    seg(ax,7.0,2.4,11.0,2.4,color='#555',lw=LW)
    seg(ax,9.0,2.4,9.0,1.9,color='#555',lw=LW)
    ax.text(9.0,1.75,'МЕХАНИЗМЫ',ha='center',fontsize=8,fontweight='bold',color='#333')

    # metadata frame
    ax.add_patch(FancyBboxPatch((.1,.0),17.8,.78,boxstyle='square,pad=0',
                                fc='#fafafa',ec='black',lw=1,zorder=1))
    fields=[('Автор:','СвойСпорт команда'),('Проект:','ProgramOnTV'),
            ('Диаграмма:','A-0 Контекстная'),('Стандарт:','IDEF0 / ГОСТ'),('Дата:','2026-05-24')]
    for i,(k,v) in enumerate(fields):
        ax.text(.3+i*3.5,.62,k,fontsize=8,fontweight='bold')
        ax.text(.3+i*3.5,.28,v,fontsize=8,color='#333')

    save(fig,'preview_03_idef0_ctx.png')


# ════════════════════════════════════════════════════════════
# 4. IDEF0 DECOMP A0  (BPWin style, B&W)
# ════════════════════════════════════════════════════════════
def preview_idef0_decomp():
    fig,ax=plt.subplots(figsize=(20,13))
    ax.set_xlim(0,20); ax.set_ylim(0,13); ax.axis('off')
    fig.patch.set_facecolor('white')

    ax.add_patch(FancyBboxPatch((.1,.5),19.8,12.1,boxstyle='square,pad=0',
                                fc='none',ec='black',lw=2,zorder=0))
    ax.text(10,12.38,'Диаграмма IDEF0 — Декомпозиция (A0)',
            ha='center',fontsize=13,fontweight='bold')
    ax.text(10,11.92,'Родительская: A-0  |  Система: «СвойСпорт TV»  |  Дата: 2026-05-24',
            ha='center',fontsize=9,color='#333')

    BW,BH=3.6,2.0
    LW=1.5

    def fbox(cx,cy,num,lines):
        ax.add_patch(FancyBboxPatch((cx-BW/2,cy-BH/2),BW,BH,
                                   boxstyle='square,pad=0',
                                   fc='white',ec='black',lw=LW,zorder=3))
        ax.text(cx+BW/2-.1,cy-BH/2+.12,num,ha='right',va='bottom',
                fontsize=8,color='#555',style='italic',zorder=4)
        for i,ln in enumerate(lines):
            off=(len(lines)-1)/2*.30-i*.30
            ax.text(cx,cy+off,ln,ha='center',va='center',
                    fontsize=10,fontweight='bold',zorder=4)

    # boxes
    fbox(3.5, 9.0,'A1',['Аутентификация','пользователя'])
    fbox(9.5, 9.0,'A2',['Загрузка каталога','трансляций'])
    fbox(3.5, 5.5,'A3',['Просмотр','трансляции'])
    fbox(9.5, 5.5,'A4',['Регистрация','сеанса просмотра'])
    fbox(15.5, 7.25,'A5',['Обработка','ошибок'])

    def ha(x1,y1,x2,y2,lbl='',lx=0,ly=.18):
        ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        if lbl:
            ax.text((x1+x2)/2+lx,(y1+y2)/2+ly,lbl,ha='center',va='center',
                    fontsize=8,bbox=dict(fc='white',ec='none',pad=.1),zorder=5)

    # ── inputs ──────────────────────────────────────────────
    ha(.3,9.2,1.7,9.2,'I1: Данные аутентификации')
    ha(.3,5.7,1.7,5.7,'I2: Команды управления плеером')

    # ── internal ─────────────────────────────────────────────
    ha(5.3,9.2,7.7,9.2,'Токен доступа')
    # A1 → A3: вниз
    seg(ax,3.5,8.0,3.5,6.5,lw=LW)
    ha(3.5,6.5,3.5,6.5,'Разрешение на просмотр',lx=1.4,ly=0)
    ax.annotate('',xy=(3.5,6.5),xytext=(3.5,8.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    # A2 → A3: URL через колено
    seg(ax,9.5,8.0,9.5,7.25,lw=LW); seg(ax,9.5,7.25,3.5,7.25,lw=LW)
    ax.annotate('',xy=(3.5,7.25),xytext=(3.5,7.25),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ha(3.5,7.25,1.7,6.5,'URL потока (HLS/DASH)',lx=0,ly=.2)
    # better: arrow from A2 down-left to A3
    seg(ax,9.5,8.0,9.5,7.0,lw=LW)
    seg(ax,9.5,7.0,5.3,7.0,lw=LW)
    ax.annotate('',xy=(5.3,6.5),xytext=(5.3,7.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(7.5,7.18,'URL потока (HLS/DASH)',ha='center',fontsize=8,
            bbox=dict(fc='white',ec='none'),zorder=5)
    # A3 → A4
    ha(5.3,5.5,7.7,5.5,'Данные сеанса (matchId, userId, ts)')
    # A2 → A4
    ha(9.5,8.0,9.5,6.5,'Выбранная трансляция',lx=.8,ly=0)
    # A3 → A5
    seg(ax,5.3,5.2,5.3,4.5,lw=LW); seg(ax,5.3,4.5,15.5,4.5,lw=LW)
    ax.annotate('',xy=(15.5,6.25),xytext=(15.5,4.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=LW))
    ax.text(10.5,4.7,'Ошибки воспроизведения',ha='center',fontsize=8,
            bbox=dict(fc='white',ec='none'),zorder=5)
    # A5 → A3: повтор
    seg(ax,15.5,8.25,15.5,10.2,lw=LW); seg(ax,15.5,10.2,3.5,10.2,lw=LW)
    seg(ax,3.5,10.2,3.5,8.0,lw=LW)
    ax.text(9.5,10.38,'Команда повтора / отмены',ha='center',fontsize=8,
            bbox=dict(fc='white',ec='none'),zorder=5)

    # ── outputs ──────────────────────────────────────────────
    ha(11.3,9.2,19.7,9.2,'O1: Каталог трансляций на экране')
    ha(5.3,5.5,19.7,5.5,'O2: Видеопоток на экране')
    ha(11.3,5.3,19.7,5.3,'O3: История просмотра',ly=-.2)
    ha(17.3,7.25,19.7,7.25,'O4: Уведомления об ошибках')

    # ── controls ─────────────────────────────────────────────
    for x,lbl2 in [(3.5,'C1: Правила\nаутентификации'),
                   (9.5,'C2: Правила доступа\nк контенту')]:
        seg(ax,x,10.0,x,11.5,lw=LW)
        ax.annotate('',xy=(x,10.0),xytext=(x,11.5),
                    arrowprops=dict(arrowstyle='->',color='black',lw=LW))
        ax.text(x,11.65,lbl2,ha='center',va='bottom',fontsize=8)

    # ── mechanisms ───────────────────────────────────────────
    for x,lbl2 in [(3.5,'M1: Android TV OS\n+ ExoPlayer'),
                   (9.5,'M2: SportTV API'),
                   (15.5,'M3: Smart TV устройство')]:
        ax.annotate('',xy=(x,4.5),xytext=(x,3.2),
                    arrowprops=dict(arrowstyle='->',color='#555',lw=1.2))
        ax.text(x,3.05,lbl2,ha='center',va='top',fontsize=8,color='#333')

    # metadata
    ax.add_patch(FancyBboxPatch((.1,.5),19.8,.9,boxstyle='square,pad=0',
                                fc='#fafafa',ec='black',lw=1,zorder=1))
    fields=[('Автор:','СвойСпорт команда'),('Проект:','ProgramOnTV'),
            ('Диаграмма:','A0 Декомпозиция'),('Стандарт:','IDEF0'),
            ('Дата:','2026-05-24')]
    for i,(k,v) in enumerate(fields):
        ax.text(.3+i*3.9,1.25,k,fontsize=8.5,fontweight='bold')
        ax.text(.3+i*3.9,.78,v,fontsize=8.5,color='#333')

    save(fig,'preview_04_idef0_decomp.png')


# ════════════════════════════════════════════════════════════
# 5. DFD  (Yourdon-DeMarco, B&W)
# ════════════════════════════════════════════════════════════
def preview_dfd():
    fig,ax=plt.subplots(figsize=(20,14))
    ax.set_xlim(0,20); ax.set_ylim(0,14); ax.axis('off')
    fig.patch.set_facecolor('white')

    ax.add_patch(FancyBboxPatch((.1,.2),19.8,13.5,boxstyle='square,pad=0',
                                fc='none',ec='black',lw=2,zorder=0))
    ax.text(10,13.55,'DFD — Диаграмма потоков данных',
            ha='center',fontsize=13,fontweight='bold')
    ax.text(10,13.1,'Процесс: «Просмотр трансляции»  |  Нотация: Yourdon–DeMarco  |  Дата: 2026-05-24',
            ha='center',fontsize=8.8,color='#333')

    def proc(cx,cy,r,num,lines):
        ax.add_patch(plt.Circle((cx,cy),r,fc='white',ec='black',lw=1.5,zorder=3))
        th=np.linspace(.22*np.pi,.78*np.pi,80)
        ax.plot(cx+r*np.cos(th),cy+r*np.sin(th),'k-',lw=1.1,zorder=4)
        ax.text(cx,cy+r*.58,num,ha='center',va='center',fontsize=7.5,fontweight='bold',zorder=5)
        for i,ln in enumerate(lines):
            off=(len(lines)-1)/2*.25-i*.25
            ax.text(cx,cy-.04+off,ln,ha='center',va='center',fontsize=8.5,zorder=5)

    def ext(cx,cy,w,h,lines):
        ax.add_patch(FancyBboxPatch((cx-w/2,cy-h/2),w,h,boxstyle='square,pad=0',
                                   fc='white',ec='black',lw=1.5,zorder=3))
        for i,ln in enumerate(lines):
            off=(len(lines)-1)/2*.25-i*.28
            ax.text(cx,cy+off,ln,ha='center',va='center',fontsize=9,fontweight='bold',zorder=4)

    def store(cx,cy,w,h,num,name):
        ax.add_patch(FancyBboxPatch((cx-w/2,cy-h/2+.02),w,h-.04,boxstyle='square,pad=0',
                                   fc='#FFFFF0',ec='none',lw=0,zorder=2))
        ax.plot([cx-w/2,cx+w/2],[cy+h/2,cy+h/2],'k-',lw=1.5,zorder=3)
        ax.plot([cx-w/2,cx+w/2],[cy-h/2,cy-h/2],'k-',lw=1.5,zorder=3)
        ax.plot([cx-w/2,cx-w/2],[cy-h/2,cy+h/2],'k-',lw=1.5,zorder=3)
        ax.text(cx-w/2+.2,cy,num,ha='left',va='center',fontsize=7.5,fontweight='bold',color='#555',zorder=4)
        ax.plot([cx-w/2+.6,cx-w/2+.6],[cy-h/2,cy+h/2],'--',color='#bbb',lw=.8,zorder=3)
        ax.text(cx-w/2+.72,cy,name,ha='left',va='center',fontsize=8.5,zorder=4)

    def fl(x1,y1,x2,y2,lbl='',rad=0,lx=0,ly=.16,fs=7.8):
        ax.annotate('',xy=(x2,y2),xytext=(x1,y1),
                    arrowprops=dict(arrowstyle='->',color='black',lw=1.3,
                                   connectionstyle=f'arc3,rad={rad}'))
        if lbl:
            ax.text((x1+x2)/2+lx,(y1+y2)/2+ly,lbl,ha='center',va='center',fontsize=fs,
                    bbox=dict(fc='white',ec='none',pad=.1),zorder=6)

    # External entities
    ext(1.2,11.5,2.1,.90,['Зритель','(Пользователь)'])
    ext(18.8,11.5,2.1,.90,['Smart TV','устройство'])
    ext(18.8, 3.5,2.1,.90,['Сервер','трансляций'])

    # Data stores
    store(10,12.5,5.0,.55,'D1','Каталог трансляций')
    store(4.5, 3.7,4.6,.55,'D2','Кэш каталога')
    store(14.5, 3.7,4.6,.55,'D3','История сеансов')

    # Processes
    proc(5.0,10.8,1.3,'P1',['Загрузка','каталога'])
    proc(5.0, 7.0,1.3,'P2',['Формирование','UI каталога'])
    proc(10.0,10.8,1.3,'P3',['Инициализация','сеанса'])
    proc(10.0, 6.5,1.4,'P4',['Управление','воспроизведением'])
    proc(14.5, 6.5,1.3,'P5',['Обработка','ошибок'])
    proc(14.5,10.8,1.3,'P6',['Регистрация','сеанса'])

    # Flows
    fl(2.25,11.5,3.7,11.0,'Запрос каталога')
    fl(2.25,11.2,8.7,11.1,'Выбор трансляции\n(matchId)',rad=-.12,ly=.2)
    fl(2.25,11.1,3.7, 7.4,'Команды навигации',rad=.15,lx=-.7,ly=0)
    fl(2.25,10.9,8.6, 7.0,'Команды плеера\n(пауза / выход)',rad=.22,lx=-.5,ly=-.12)

    fl(6.3,11.0,17.7,11.8,'HTTP-запрос (SportTV API)',rad=-.08,ly=.2)
    fl(17.7,11.5,6.3,10.6,'JSON: список матчей',rad=-.08,ly=-.22)
    fl(11.3,11.0,17.7,11.2,'Запрос URL потока',rad=-.08,ly=.2)
    fl(17.7,10.9,11.3,10.6,'URL потока (HLS/DASH)',rad=-.08,ly=-.22)

    fl(5.5,12.15,7.5,12.35,'Сохранить в каталог',ly=.2)
    fl(7.5,12.15,5.5,12.0,'Данные каталога',ly=-.22)
    fl(5.0,9.5,5.0,8.3,'Данные каталога')
    fl(5.0,9.5,5.0,3.97,'Кэшировать',lx=-1.0,ly=0)

    seg(ax,5.0,9.5,4.5,9.5,lw=1.2); dot(ax,5.0,9.5)
    ax.annotate('',xy=(4.5,3.97),xytext=(4.5,9.5),
                arrowprops=dict(arrowstyle='->',color='black',lw=1.2))
    ax.text(3.8,6.8,'Кэшировать\nкаталог',ha='center',fontsize=7.8,
            bbox=dict(fc='white',ec='none'),zorder=5)

    fl(4.5,3.97,5.0,5.7,'Данные из кэша')
    fl(10.0,9.5,10.0,7.9,'Параметры потока\n(URL, title, isLive)')
    fl(10.0,9.5,14.5,9.5,'Данные сеанса\n(matchId, userId, ts)')
    fl(14.5,9.5,14.5,3.97,'Запись сеанса')
    fl(15.0,3.97,15.0,9.5,'Подтверждение\nзаписи',lx=.6)
    seg(ax,15.0,3.97,15.0,9.5,lw=1.1); dot(ax,15.0,3.97)

    fl(11.4, 6.5,13.2, 6.5,'Событие ошибки')
    fl(13.2, 6.2,11.4, 6.2,'Команда повтора',rad=-.12,ly=-.22)
    fl(10.0, 5.2,17.7, 3.8,'Видеопоток на экране',rad=.1,lx=.5,ly=-.1)
    fl(15.8, 6.5,17.7,11.0,'Уведомление об ошибке',rad=.1,lx=.5,ly=0)
    fl(5.0,  5.8,17.7,11.2,'Экран каталога (Compose UI)',rad=.08,lx=1.0,ly=.2)

    # Legend
    ax.text(.3,3.2,'Обозначения:',fontsize=8.5,fontweight='bold')
    ax.add_patch(plt.Circle((.75,2.6),.28,fc='white',ec='black',lw=1.3))
    ax.text(.75,2.6,'P',ha='center',va='center',fontsize=7)
    ax.text(1.15,2.6,'— Процесс',fontsize=7.8,va='center')
    ax.add_patch(FancyBboxPatch((.45,2.0),.6,.38,boxstyle='square,pad=0',
                                fc='white',ec='black',lw=1.3))
    ax.text(.75,2.19,'E',ha='center',va='center',fontsize=7)
    ax.text(1.15,2.19,'— Внешняя сущность',fontsize=7.8,va='center')
    ax.plot([.45,1.05],[1.65,1.65],'k-',lw=1.3); ax.plot([.45,1.05],[1.35,1.35],'k-',lw=1.3)
    ax.plot([.45,.45],[1.35,1.65],'k-',lw=1.3)
    ax.text(1.15,1.5,'— Хранилище данных',fontsize=7.8,va='center')
    ax.annotate('',xy=(1.05,1.0),xytext=(.45,1.0),
                arrowprops=dict(arrowstyle='->',color='black',lw=1.3))
    ax.text(1.15,1.0,'— Поток данных',fontsize=7.8,va='center')

    save(fig,'preview_05_dfd.png')


if __name__=='__main__':
    preview_usecase()
    preview_state()
    preview_idef0_ctx()
    preview_idef0_decomp()
    preview_dfd()
