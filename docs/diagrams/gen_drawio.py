"""
Генератор draw.io (.drawio) файлов для проекта «СвойСпорт TV»
Импорт в draw.io: File → Import From → Device
"""
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# ── low-level helpers ────────────────────────────────────────

def xml_wrap(cells, pw=1654, ph=1169, title=''):
    return (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        f'<mxGraphModel dx="1422" dy="762" grid="1" gridSize="10" guides="1" '
        f'tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" '
        f'pageWidth="{pw}" pageHeight="{ph}" math="0" shadow="0">\n'
        '  <root>\n'
        '    <mxCell id="0"/>\n'
        '    <mxCell id="1" parent="0"/>\n'
        + ''.join(cells) +
        '  </root>\n</mxGraphModel>'
    )

def esc(s):
    return s.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;').replace('"','&quot;')

def vx(id, value, style, x, y, w, h, parent='1'):
    return (f'    <mxCell id="{id}" value="{esc(value)}" style="{style}" '
            f'vertex="1" parent="{parent}">\n'
            f'      <mxGeometry x="{x}" y="{y}" width="{w}" height="{h}" as="geometry"/>\n'
            f'    </mxCell>\n')

def ex(id, value, style, src='', tgt='', x1=None, y1=None, x2=None, y2=None,
       pts=None, parent='1'):
    src_attr = f' source="{src}"' if src else ''
    tgt_attr = f' target="{tgt}"' if tgt else ''
    geo_inner = ''
    if x1 is not None:
        geo_inner += f'\n        <mxPoint x="{x1}" y="{y1}" as="sourcePoint"/>'
    if x2 is not None:
        geo_inner += f'\n        <mxPoint x="{x2}" y="{y2}" as="targetPoint"/>'
    if pts:
        geo_inner += '\n        <Array as="points">'
        for p in pts:
            geo_inner += f'<mxPoint x="{p[0]}" y="{p[1]}"/>'
        geo_inner += '</Array>'
    return (f'    <mxCell id="{id}" value="{esc(value)}" style="{style}" '
            f'edge="1"{src_attr}{tgt_attr} parent="{parent}">\n'
            f'      <mxGeometry relative="1" as="geometry">{geo_inner}\n'
            f'      </mxGeometry>\n'
            f'    </mxCell>\n')

# common styles
S_ACTOR   = 'shape=mxgraph.uml2.actor;html=1;whiteSpace=wrap;fillColor=#ffffff;strokeColor=#000000;'
S_UC      = 'ellipse;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;fontSize=11;'
S_BOUND   = ('rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#000000;'
             'fontStyle=1;fontSize=12;verticalAlign=top;')
S_ASSOC   = 'endArrow=none;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;'
S_ASSOC2  = 'endArrow=none;html=1;'
S_INCL    = ('dashed=1;endArrow=open;endFill=0;html=1;startArrow=none;startFill=0;'
             'fontSize=10;fontStyle=2;')
S_EXTN    = ('dashed=1;endArrow=open;endFill=0;html=1;startArrow=none;startFill=0;'
             'fontSize=10;fontStyle=2;')
S_STATE   = ('rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;'
             'fontSize=11;fontStyle=1;')
S_STATE_A = ('rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;'
             'fontSize=10;fontStyle=0;')  # with action text
S_INIT    = 'ellipse;aspect=fixed;html=1;fillColor=#000000;strokeColor=#000000;'
S_FINAL   = 'doubleEllipse;aspect=fixed;html=1;fillColor=#000000;strokeColor=#000000;'
S_TR      = 'endArrow=block;endFill=1;html=1;fontSize=10;'
S_TR_D    = 'endArrow=block;endFill=1;html=1;fontSize=10;dashed=1;'
S_BOX     = ('rounded=0;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#000000;'
             'fontStyle=1;fontSize=12;verticalAlign=middle;')
S_BOX_W   = ('rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;'
             'fontStyle=1;fontSize=12;verticalAlign=middle;')
S_ARR     = 'endArrow=block;endFill=1;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;fontSize=10;'
S_ARR2    = 'endArrow=block;endFill=1;html=1;fontSize=10;'
S_PROC    = 'ellipse;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#000000;fontSize=11;'
S_EXT     = 'rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#000000;fontStyle=1;fontSize=11;'
S_STORE   = ('shape=mxgraph.flowchart.stored_data;whiteSpace=wrap;html=1;'
             'fillColor=#fff9c4;strokeColor=#000000;fontSize=10;')
S_TITLE   = 'text;html=1;strokeColor=none;fillColor=none;align=center;fontStyle=1;fontSize=14;'
S_NOTE    = 'text;html=1;strokeColor=none;fillColor=none;align=left;fontSize=10;'


# ════════════════════════════════════════════════════════════
# 1. USE CASE DIAGRAM
# ════════════════════════════════════════════════════════════
def gen_usecase():
    c = []
    # Title
    c.append(vx('t0','Диаграмма вариантов использования\nСистема «СвойСпорт TV»',
                S_TITLE, 200, 10, 1000, 50))

    # System boundary
    c.append(vx('sb','Приложение «СвойСпорт TV»', S_BOUND, 180, 70, 980, 1160))

    # ── Actors ──────────────────────────────────────────────
    c.append(vx('a_v','Зритель',   S_ACTOR, 30,  600, 60, 90))
    c.append(vx('a_a','Администратор', S_ACTOR, 1240, 260, 60, 90))

    # ── Use cases — Зритель ─────────────────────────────────
    # (id, label, x, y, w, h)
    viewer_ucs = [
        ('uc1',  'Запустить\nприложение',           280, 100, 210, 60),
        ('uc2',  'Аутентифицироваться\n(ввести код)',280, 195, 210, 60),
        ('uc3',  'Просмотреть\nглавный экран',       280, 290, 210, 60),
        ('uc4',  'Получить список\nтрансляций',      280, 385, 210, 60),
        ('uc5',  'Просмотреть\nкаталог',             280, 480, 210, 60),
        ('uc6',  'Выбрать\nтрансляцию',              280, 575, 210, 60),
        ('uc7',  'Просмотреть\nдетали матча',        280, 670, 210, 60),
        ('uc8',  'Запустить\nвоспроизведение',       280, 765, 210, 60),
        ('uc10', 'Управлять\nвоспроизведением',      280, 875, 210, 60),
        ('uc12', 'Просмотреть\nрасписание',          280, 975, 210, 60),
        ('uc13', 'Получить уведомление\nоб ошибке',  280,1075, 230, 60),
    ]
    for uid, lbl, x, y, w, h in viewer_ucs:
        c.append(vx(uid, lbl, S_UC, x, y, w, h))
        # association line from actor to UC
        c.append(ex(f'e_{uid}', '', S_ASSOC2, 'a_v', uid))

    # ── Include / Extend ────────────────────────────────────
    c.append(vx('uc9',  'Загрузить\nHLS-поток',  S_UC, 580, 780, 200, 60))
    c.append(vx('uc11', 'Поставить\nна паузу',   S_UC, 580, 890, 200, 60))

    c.append(ex('e_inc', '«include»', S_INCL, 'uc8',  'uc9'))
    c.append(ex('e_ext', '«extend»',  S_EXTN, 'uc10', 'uc11'))

    # ── Use cases — Администратор ────────────────────────────
    admin_ucs = [
        ('uca1', 'Настроить подключение\nк серверу трансляций', 820, 145, 240, 65),
        ('uca2', 'Управлять каталогом\nтрансляций',             820, 240, 240, 65),
        ('uca3', 'Провести тестовый\nзапуск трансляции',        820, 335, 240, 65),
    ]
    for uid, lbl, x, y, w, h in admin_ucs:
        c.append(vx(uid, lbl, S_UC, x, y, w, h))
        c.append(ex(f'e_{uid}', '', S_ASSOC2, 'a_a', uid))

    with open(f'{OUT}/01_use_case.drawio', 'w', encoding='utf-8') as f:
        f.write(xml_wrap(c, pw=1400, ph=1300))
    print('✓ 01_use_case.drawio')


# ════════════════════════════════════════════════════════════
# 2. STATE DIAGRAM
# ════════════════════════════════════════════════════════════
def gen_state():
    c = []
    c.append(vx('t0','Диаграмма состояний сеанса просмотра трансляции\nПриложение «СвойСпорт TV»',
                S_TITLE, 100, 10, 980, 50))

    W, H = 280, 70

    def st(id, name, action, x, y):
        label = f'<b>{name}</b><br/><font style="font-size:9px;font-style:italic;">{action}</font>'
        c.append(vx(id, label, S_STATE_A.replace('fontStyle=0',''), x, y, W, H))

    def tr(id, lbl, src, tgt, rad='0', ex_x=None, ex_y=None,
           en_x=None, en_y=None, pts=None):
        style = (f'endArrow=block;endFill=1;html=1;fontSize=10;'
                 f'curved=0;exitX={ex_x};exitY={ex_y};exitDx=0;exitDy=0;'
                 f'entryX={en_x};entryY={en_y};entryDx=0;entryDy=0;') \
                if ex_x is not None else \
                'endArrow=block;endFill=1;html=1;fontSize=10;'
        c.append(ex(id, lbl, style, src, tgt, pts=pts))

    # ── States ──────────────────────────────────────────────
    st('s1','Инициализация',      'entry / запуск PlayerManager',           360,  90)
    st('s2','Загрузка параметров','do / HTTP-запрос URL потока к SportTV API',360, 225)
    st('s3','Буферизация',        'do / ExoPlayer.prepare()',               360, 360)
    st('s4','Воспроизведение',    'do / ExoPlayer.play()  entry / показ UI',120, 495)
    st('s5','Пауза',              'do / ExoPlayer.pause()',                 120, 635)
    st('s6','Ошибка',             'entry / показать сообщение об ошибке',   620, 495)
    st('s7','Повтор подключения', 'do / retry  max 3 попытки',              620, 635)
    st('s8','Завершён',           'exit / PlayerManager.release()',         360, 775)

    # Initial / final
    c.append(vx('ini','', S_INIT, 480, 30, 30, 30))
    c.append(vx('fin','', S_FINAL, 480, 905, 30, 30))

    # ── Transitions ─────────────────────────────────────────
    c.append(ex('t01','Запуск плеера',          'endArrow=block;endFill=1;html=1;', 'ini','s1'))
    c.append(ex('t12','matchId получен',         'endArrow=block;endFill=1;html=1;', 's1','s2'))
    c.append(ex('t23','URL потока получен',       'endArrow=block;endFill=1;html=1;', 's2','s3'))
    c.append(ex('t34','Буфер заполнен',          'endArrow=block;endFill=1;html=1;', 's3','s4'))
    c.append(ex('t36','Таймаут / сервер недоступен',
                'endArrow=block;endFill=1;html=1;curved=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;',
                's3','s6'))
    # play ↔ pause
    c.append(ex('t45','Команда «Пауза»',
                'endArrow=block;endFill=1;html=1;exitX=0;exitY=0.5;exitDx=0;exitDy=0;'
                'entryX=0;entryY=0.5;entryDx=0;entryDy=0;curved=1;',
                's4','s5'))
    c.append(ex('t54','Команда «Продолжить»',
                'endArrow=block;endFill=1;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;'
                'entryX=1;entryY=0.5;entryDx=0;entryDy=0;curved=1;',
                's5','s4'))
    # play → error
    c.append(ex('t46','Потеря соединения / ошибка потока',
                'endArrow=block;endFill=1;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;'
                'entryX=0;entryY=0.5;entryDx=0;entryDy=0;',
                's4','s6'))
    # error ↔ retry
    c.append(ex('t67','Повторная попытка',
                'endArrow=block;endFill=1;html=1;exitX=1;exitY=0.5;exitDx=0;exitDy=0;'
                'entryX=1;entryY=0.5;entryDx=0;entryDy=0;curved=1;',
                's6','s7'))
    c.append(ex('t73','Соединение восстановлено',
                'endArrow=block;endFill=1;html=1;curved=1;exitX=0;exitY=0;exitDx=0;exitDy=0;'
                'entryX=1;entryY=1;entryDx=0;entryDy=0;',
                's7','s3'))
    # → завершён
    c.append(ex('t48','Команда «Выход»',
                'endArrow=block;endFill=1;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;'
                'entryX=0;entryY=0.5;entryDx=0;entryDy=0;',
                's4','s8'))
    c.append(ex('t58','Команда «Выход»',
                'endArrow=block;endFill=1;html=1;exitX=0.5;exitY=1;exitDx=0;exitDy=0;'
                'entryX=0;entryY=1;entryDx=0;entryDy=0;curved=1;',
                's5','s8'))
    c.append(ex('t68','Отмена',
                'endArrow=block;endFill=1;html=1;exitX=0;exitY=1;exitDx=0;exitDy=0;'
                'entryX=1;entryY=0.5;entryDx=0;entryDy=0;curved=1;',
                's6','s8'))
    c.append(ex('t78','Превышен лимит попыток',
                'endArrow=block;endFill=1;html=1;exitX=0;exitY=1;exitDx=0;exitDy=0;'
                'entryX=1;entryY=1;entryDx=0;entryDy=0;curved=1;',
                's7','s8'))
    c.append(ex('t8f','', 'endArrow=block;endFill=1;html=1;', 's8','fin'))

    with open(f'{OUT}/02_state_diagram.drawio', 'w', encoding='utf-8') as f:
        f.write(xml_wrap(c, pw=1000, ph=1000))
    print('✓ 02_state_diagram.drawio')


# ════════════════════════════════════════════════════════════
# 3. IDEF0 CONTEXT A-0  (BPWin style)
# ════════════════════════════════════════════════════════════
def gen_idef0_ctx():
    c = []
    PW, PH = 1654, 1169

    # Outer frame
    c.append(vx('frame','', 'rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#000000;',
                10, 10, PW-20, PH-150))

    # Title area (top)
    c.append(vx('ttl',
                'Диаграмма IDEF0 — Контекстная диаграмма (A-0)\n'
                'Система: «СвойСпорт TV»   Назначение: просмотр спортивных трансляций на Smart TV',
                'text;html=1;strokeColor=none;fillColor=none;align=center;fontStyle=1;fontSize=13;',
                10, 15, PW-20, 60))

    # Main function box
    BX, BY, BW, BH = 577, 300, 500, 300
    c.append(vx('mbox',
                'A-0\n\nОбеспечить просмотр\nспортивных трансляций\nна Smart TV устройстве',
                'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;'
                'fontStyle=1;fontSize=13;verticalAlign=middle;',
                BX, BY, BW, BH))

    ARR = 'endArrow=block;endFill=1;html=1;strokeColor=#000000;fontSize=11;fontStyle=0;'
    ARR_UP = ('endArrow=block;endFill=1;html=1;strokeColor=#555555;fontSize=11;'
              'fontStyle=0;strokeWidth=1.5;')

    # ── INPUTS (left → box) ──────────────────────────────────
    inputs = [
        ('i1', 'I1: Запрос пользователя\n(навигация, выбор трансляции)', 30, 380),
        ('i2', 'I2: Команды управления\nплеером (пауза, выход)',          30, 490),
    ]
    for nid, lbl, x, y in inputs:
        c.append(ex(f'e_{nid}', lbl, ARR, x1=x, y1=y, x2=BX, y2=y))
    # entry label
    c.append(vx('lbl_in','ВХОД',
                'text;html=1;strokeColor=none;fillColor=none;align=center;'
                'fontStyle=1;fontSize=10;rotation=-90;',
                5, 435, 80, 30))

    # ── OUTPUTS (box → right) ────────────────────────────────
    outputs = [
        ('o1', 'O1: Видеопоток на экране (HLS/DASH)', BX+BW, 350),
        ('o2', 'O2: UI каталога и навигации',          BX+BW, 430),
        ('o3', 'O3: Уведомления об ошибках',           BX+BW, 510),
    ]
    for nid, lbl, x, y in outputs:
        c.append(ex(f'e_{nid}', lbl, ARR, x1=x, y1=y, x2=PW-80, y2=y))
    c.append(vx('lbl_out','ВЫХОД',
                'text;html=1;strokeColor=none;fillColor=none;align=center;'
                'fontStyle=1;fontSize=10;rotation=-90;',
                PW-65, 430, 80, 30))

    # ── CONTROLS (top → box) ────────────────────────────────
    controls = [
        ('c1', 'C1: Требования к качеству\nвидео (битрейт, разрешение)', 630),
        ('c2', 'C2: Правила доступа\nк трансляциям',                      827),
        ('c3', 'C3: Протоколы\nHLS / DASH',                              1020),
    ]
    for nid, lbl, x in controls:
        c.append(ex(f'e_{nid}', lbl, ARR, x1=x, y1=60, x2=x, y2=BY))
    c.append(vx('lbl_ctrl','УПРАВЛЕНИЕ',
                'text;html=1;strokeColor=none;fillColor=none;align=center;'
                'fontStyle=1;fontSize=10;',
                700, 45, 130, 25))

    # ── MECHANISMS (box → bottom, upward arrow) ──────────────
    mechs = [
        ('m1', 'M1: Android TV OS\n+ ExoPlayer',       660),
        ('m2', 'M2: Сервер трансляций\n(SportTV API)',  827),
        ('m3', 'M3: Smart TV\nустройство (аппаратура)', 990),
    ]
    for nid, lbl, x in mechs:
        c.append(ex(f'e_{nid}', lbl, ARR_UP, x1=x, y1=680, x2=x, y2=BY+BH))
    c.append(vx('lbl_mech','МЕХАНИЗМЫ',
                'text;html=1;strokeColor=none;fillColor=none;align=center;'
                'fontStyle=1;fontSize=10;',
                760, 685, 130, 25))

    # ── Metadata frame (bottom) ──────────────────────────────
    c.append(vx('meta_frame','',
                'rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#000000;',
                10, PH-145, PW-20, 135))
    fields = [
        ('Автор:',      'СвойСпорт команда'),
        ('Проект:',     'ProgramOnTV'),
        ('Диаграмма:',  'A-0 Контекстная'),
        ('Стандарт:',   'IDEF0 / ГОСТ Р ИСО 10746'),
        ('Дата:',       '2026-05-24'),
        ('Версия:',     '1.0'),
    ]
    for i, (k, v2) in enumerate(fields):
        fx = 20 + i * 265
        c.append(vx(f'mk{i}', k,
                    'text;html=1;strokeColor=none;fillColor=none;align=left;fontStyle=1;fontSize=11;',
                    fx, PH-135, 260, 30))
        c.append(vx(f'mv{i}', v2,
                    'text;html=1;strokeColor=none;fillColor=none;align=left;fontSize=11;',
                    fx, PH-105, 260, 30))

    with open(f'{OUT}/03_idef0_context.drawio', 'w', encoding='utf-8') as f:
        f.write(xml_wrap(c, pw=PW, ph=PH))
    print('✓ 03_idef0_context.drawio')


# ════════════════════════════════════════════════════════════
# 4. IDEF0 DECOMP A0  (BPWin style)
# ════════════════════════════════════════════════════════════
def gen_idef0_decomp():
    c = []
    PW, PH = 1920, 1200

    # outer frame
    c.append(vx('frame','',
                'rounded=0;whiteSpace=wrap;html=1;fillColor=none;strokeColor=#000000;',
                10, 10, PW-20, PH-150))
    c.append(vx('ttl',
                'Диаграмма IDEF0 — Декомпозиция (A0)\n'
                'Родительская: A-0   Система: «СвойСпорт TV»   Дата: 2026-05-24',
                'text;html=1;strokeColor=none;fillColor=none;align=center;fontStyle=1;fontSize=13;',
                10, 12, PW-20, 55))

    BW, BH = 280, 200
    ARR = ('endArrow=block;endFill=1;html=1;strokeColor=#000000;'
           'fontSize=10;fontStyle=0;strokeWidth=1.5;')

    # ── Function boxes ───────────────────────────────────────
    #    (id, label, x, y)
    boxes = [
        ('a1', 'A1\nАутентификация\nпользователя',     150,  200),
        ('a2', 'A2\nЗагрузка каталога\nтрансляций',    700,  200),
        ('a3', 'A3\nПросмотр\nтрансляции',             150,  600),
        ('a4', 'A4\nРегистрация\nсеанса просмотра',    700,  600),
        ('a5', 'A5\nОбработка ошибок\nи уведомлений', 1250,  400),
    ]
    for bid, lbl, x, y in boxes:
        c.append(vx(bid, lbl,
                    'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#000000;'
                    'fontStyle=1;fontSize=12;verticalAlign=middle;',
                    x, y, BW, BH))

    # ── Left boundary inputs ─────────────────────────────────
    # I1 → A1
    c.append(ex('e_i1a1','I1: Данные аутентификации\n(email / код)', ARR,
                x1=10, y1=270, x2=150, y2=270))
    # I2 → A3
    c.append(ex('e_i2a3','I2: Команды управления\nплеером', ARR,
                x1=10, y1=670, x2=150, y2=670))

    # ── Internal flows ───────────────────────────────────────
    # A1 → A2: токен доступа
    c.append(ex('e_a1a2','Токен доступа', ARR,
                x1=430, y1=270, x2=700, y2=270))
    # A1 → A3: разрешение на просмотр
    c.append(ex('e_a1a3','Разрешение\nна просмотр', ARR,
                x1=290, y1=400, x2=290, y2=600))
    # A2 → A3: URL потока
    c.append(ex('e_a2a3','URL потока (HLS/DASH)', ARR,
                x1=840, y1=400, x2=840, y2=540,
                pts=[(840,540),(290,540),(290,600)]))
    # A2 → A4: выбранная трансляция
    c.append(ex('e_a2a4','Выбранная трансляция', ARR,
                x1=840, y1=400, x2=840, y2=600))
    # A3 → A4: данные сеанса
    c.append(ex('e_a3a4','Данные сеанса\n(matchId, userId, timestamp)', ARR,
                x1=430, y1=670, x2=700, y2=700))
    # A3 → A5: ошибки
    c.append(ex('e_a3a5','Ошибки воспроизведения', ARR,
                x1=430, y1=650, x2=1250, y2=500))
    # A5 → A3: команда повтора/отмены
    c.append(ex('e_a5a3','Команда повтора / отмены', ARR,
                x1=1250, y1=550, x2=580, y2=700,
                pts=[(700,700)]))

    # ── Right boundary outputs ───────────────────────────────
    # A2 → каталог на экране
    c.append(ex('e_o1','O1: Каталог трансляций\nна экране', ARR,
                x1=980, y1=250, x2=PW-50, y2=250))
    # A3 → видеопоток
    c.append(ex('e_o2','O2: Видеопоток\nна экране', ARR,
                x1=430, y1=630, x2=PW-50, y2=630))
    # A4 → история
    c.append(ex('e_o3','O3: История просмотра', ARR,
                x1=980, y1=700, x2=PW-50, y2=700))
    # A5 → уведомления
    c.append(ex('e_o4','O4: Уведомления\nоб ошибках', ARR,
                x1=1530, y1=460, x2=PW-50, y2=460))

    # ── Top controls ─────────────────────────────────────────
    c.append(ex('e_c1','C1: Правила аутентификации', ARR,
                x1=290, y1=60, x2=290, y2=200))
    c.append(ex('e_c2','C2: Правила доступа к контенту', ARR,
                x1=840, y1=60, x2=840, y2=200))
    c.append(ex('e_c3','C3: Протоколы HLS/DASH', ARR,
                x1=290, y1=130, x2=80, y2=130,
                pts=[(80,630),(150,630)]))

    # ── Bottom mechanisms ────────────────────────────────────
    ARR_UP = ('endArrow=block;endFill=1;html=1;strokeColor=#555555;'
              'fontSize=10;fontStyle=0;strokeWidth=1.5;')
    mechs = [
        ('e_m1','M1: Android TV OS + ExoPlayer', 220, PH-190, 220, 800),
        ('e_m2','M2: SportTV API',                840, PH-190, 840, 800),
        ('e_m3','M3: Smart TV устройство',       1390, PH-190,1390, 600),
    ]
    for mid, lbl, x1, y1, x2, y2 in mechs:
        c.append(ex(mid, lbl, ARR_UP, x1=x1, y1=y1, x2=x2, y2=y2))

    # ── Metadata ─────────────────────────────────────────────
    c.append(vx('meta','',
                'rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#000000;',
                10, PH-145, PW-20, 135))
    fields = [('Автор:','СвойСпорт команда'),('Проект:','ProgramOnTV'),
              ('Диаграмма:','A0 Декомпозиция'),('Стандарт:','IDEF0'),
              ('Дата:','2026-05-24'),('Версия:','1.0')]
    for i,(k,v2) in enumerate(fields):
        fx = 20 + i*310
        c.append(vx(f'mk{i}',k,'text;html=1;strokeColor=none;fillColor=none;'
                    'align=left;fontStyle=1;fontSize=11;', fx, PH-135, 300, 30))
        c.append(vx(f'mv{i}',v2,'text;html=1;strokeColor=none;fillColor=none;'
                    'align=left;fontSize=11;', fx, PH-105, 300, 30))

    with open(f'{OUT}/04_idef0_decomp.drawio', 'w', encoding='utf-8') as f:
        f.write(xml_wrap(c, pw=PW, ph=PH))
    print('✓ 04_idef0_decomp.drawio')


# ════════════════════════════════════════════════════════════
# 5. DFD — «Просмотр трансляции» (Yourdon-DeMarco)
# ════════════════════════════════════════════════════════════
def gen_dfd():
    c = []
    PW, PH = 1800, 1200

    c.append(vx('t0',
                'DFD — Диаграмма потоков данных\n'
                'Процесс: «Просмотр трансляции»   Нотация: Yourdon–DeMarco   '
                'Система: «СвойСпорт TV»   Дата: 2026-05-24',
                'text;html=1;strokeColor=none;fillColor=none;align=center;fontStyle=1;fontSize=12;',
                10, 8, PW-20, 50))

    ARR = 'endArrow=block;endFill=1;html=1;fontSize=10;strokeWidth=1.5;'
    ARR_C= 'endArrow=block;endFill=1;html=1;fontSize=10;strokeWidth=1.5;curved=1;'

    PROC  = ('ellipse;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#000000;'
             'fontSize=11;fontStyle=0;')
    EXT   = ('rounded=0;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#000000;'
             'fontStyle=1;fontSize=11;')
    STORE = ('shape=mxgraph.flowchart.stored_data;whiteSpace=wrap;html=1;'
             'fillColor=#fff9c4;strokeColor=#000000;fontSize=10;')

    # ── External entities ────────────────────────────────────
    c.append(vx('ext_v', 'Зритель\n(Пользователь)', EXT,  30, 480, 160, 80))
    c.append(vx('ext_tv','Smart TV\nустройство',     EXT,1600, 250, 160, 80))
    c.append(vx('ext_sv','Сервер\nтрансляций',       EXT,1600, 700, 160, 80))

    # ── Data stores ──────────────────────────────────────────
    c.append(vx('d1','D1   Каталог трансляций', STORE, 550, 80, 300, 60))
    c.append(vx('d2','D2   Кэш каталога',       STORE, 220, 950, 300, 60))
    c.append(vx('d3','D3   История сеансов',    STORE,1050, 950, 300, 60))

    # ── Processes (circles 160x160) ──────────────────────────
    c.append(vx('p1','P1\nЗагрузка\nкаталога',          PROC, 350, 200, 160, 160))
    c.append(vx('p2','P2\nФормирование\nUI каталога',    PROC, 220, 580, 160, 160))
    c.append(vx('p3','P3\nИнициализация\nсеанса',        PROC, 750, 350, 160, 160))
    c.append(vx('p4','P4\nУправление\nвоспроизведением', PROC, 750, 620, 160, 160))
    c.append(vx('p5','P5\nОбработка\nошибок',            PROC,1150, 580, 160, 160))
    c.append(vx('p6','P6\nРегистрация\nсеанса',          PROC,1150, 350, 160, 160))

    # ── Flows ────────────────────────────────────────────────
    # Зритель → P1
    c.append(ex('f01','Запрос каталога', ARR, 'ext_v','p1'))
    # Зритель → P3
    c.append(ex('f02','Выбор трансляции (matchId)', ARR_C, 'ext_v','p3'))
    # Зритель → P2
    c.append(ex('f03','Команды навигации', ARR, 'ext_v','p2'))
    # Зритель → P4
    c.append(ex('f04','Команды плеера\n(пауза / выход)', ARR_C, 'ext_v','p4'))

    # P1 ↔ Сервер
    c.append(ex('f05','HTTP-запрос (SportTV API)', ARR_C, 'p1','ext_sv'))
    c.append(ex('f06','JSON: список матчей',        ARR_C, 'ext_sv','p1'))
    # P3 ↔ Сервер
    c.append(ex('f07','Запрос URL потока (matchId)', ARR_C, 'p3','ext_sv'))
    c.append(ex('f08','URL потока (HLS/DASH)',        ARR_C, 'ext_sv','p3'))

    # P1 → D1
    c.append(ex('f09','Сохранить данные каталога', ARR, 'p1','d1'))
    # D1 → P1
    c.append(ex('f10','Данные каталога', ARR_C, 'd1','p1'))
    # P1 → D2
    c.append(ex('f11','Кэшировать каталог', ARR, 'p1','d2'))
    # D2 → P2
    c.append(ex('f12','Данные из кэша', ARR, 'd2','p2'))
    # P1 → P2
    c.append(ex('f13','Данные каталога', ARR, 'p1','p2'))

    # P2 → Smart TV
    c.append(ex('f14','Экран каталога (Compose UI)', ARR_C, 'p2','ext_tv'))

    # P3 → P4
    c.append(ex('f15','Параметры потока\n(URL, title, isLive)', ARR, 'p3','p4'))
    # P3 → P6
    c.append(ex('f16','Данные сеанса\n(matchId, userId, ts)', ARR, 'p3','p6'))

    # P6 → D3
    c.append(ex('f17','Запись сеанса', ARR, 'p6','d3'))
    # D3 → P6
    c.append(ex('f18','Подтверждение записи', ARR_C, 'd3','p6'))

    # P4 → Smart TV
    c.append(ex('f19','Видеопоток на экране', ARR_C, 'p4','ext_tv'))
    # P4 → P5
    c.append(ex('f20','Событие ошибки (код, тип)', ARR, 'p4','p5'))
    # P5 → P4
    c.append(ex('f21','Команда повтора', ARR_C, 'p5','p4'))
    # P5 → Smart TV
    c.append(ex('f22','Уведомление об ошибке', ARR_C, 'p5','ext_tv'))

    # Legend
    c.append(vx('lg_t','Обозначения:',
                'text;html=1;strokeColor=none;fillColor=none;align=left;fontStyle=1;fontSize=11;',
                30, 1050, 200, 30))
    c.append(vx('lg_p','Процесс', PROC, 30, 1085, 80, 60))
    c.append(vx('lg_e','Внешняя\nсущность', EXT,  140, 1090, 100, 50))
    c.append(vx('lg_s','D   Хранилище', STORE, 270, 1088, 180, 55))

    with open(f'{OUT}/05_dfd.drawio', 'w', encoding='utf-8') as f:
        f.write(xml_wrap(c, pw=PW, ph=PH))
    print('✓ 05_dfd.drawio')


# ════════════════════════════════════════════════════════════
if __name__ == '__main__':
    gen_usecase()
    gen_state()
    gen_idef0_ctx()
    gen_idef0_decomp()
    gen_dfd()
    print(f'\nФайлы .drawio сохранены в: {OUT}')
    print('Импорт в draw.io: меню File → Import From → Device')
