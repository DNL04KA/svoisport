"""Трёхуровневая архитектура клиент-серверной системы (для раздела 5.2.1)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

fig, ax = plt.subplots(figsize=(14, 10))
ax.set_xlim(0, 14)
ax.set_ylim(0, 10)
ax.axis('off')
fig.patch.set_facecolor('white')

BORDER = '#1A3A5C'
TXT    = '#1A1A1A'

def box(cx, cy, w, h, title, items=None, fc='#E8EEF4', title_fs=10, item_fs=8.5):
    rect = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                          boxstyle='round,pad=0.15',
                          facecolor=fc, edgecolor=BORDER, linewidth=2.0, zorder=3)
    ax.add_patch(rect)
    if items:
        ax.plot([cx - w/2 + 0.1, cx + w/2 - 0.1],
                [cy + h/2 - 0.5, cy + h/2 - 0.5],
                color=BORDER, lw=1.0, zorder=4)
        ax.text(cx, cy + h/2 - 0.25, title, ha='center', va='center',
                fontsize=title_fs, fontweight='bold', color=TXT, zorder=5)
        step = (h - 0.65) / (len(items) + 0.5)
        for k, itm in enumerate(items):
            iy = cy + h/2 - 0.65 - step * (k + 0.7)
            ax.text(cx, iy, f'• {itm}', ha='center', va='center',
                    fontsize=item_fs, color='#333333', zorder=5)
    else:
        ax.text(cx, cy, title, ha='center', va='center',
                fontsize=title_fs, fontweight='bold', color=TXT, zorder=5)

def arrow(x1, y1, x2, y2, label='', bidirectional=False, lw=1.8):
    style = '<->' if bidirectional else '->'
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle=style, color=BORDER, lw=lw,
                                connectionstyle='arc3,rad=0.0'))
    if label:
        ax.text((x1+x2)/2 + 0.15, (y1+y2)/2 + 0.18, label,
                ha='center', va='center', fontsize=8, color='#444444',
                bbox=dict(boxstyle='round,pad=0.12', facecolor='white',
                          edgecolor='none', alpha=0.9))

# =================== TITLE ===================
ax.text(7.0, 9.65, 'Архитектура системы управления спортивными трансляциями',
        ha='center', va='center', fontsize=13, fontweight='bold', color=TXT)

# =================== CLIENT SIDE ===================
# Большой контейнер клиента
client_rect = FancyBboxPatch((0.3, 1.0), 5.8, 8.0,
                             boxstyle='round,pad=0.2',
                             facecolor='#EBF5FB', edgecolor='#2471A3',
                             linewidth=2.5, linestyle='--', zorder=1)
ax.add_patch(client_rect)
ax.text(3.2, 9.1, 'Клиентская часть (Android TV)', ha='center',
        fontsize=9.5, color='#2471A3', fontweight='bold')

box(3.2, 7.5, 5.0, 1.4,
    'UI Layer — Presentation',
    ['HomeScreen, DetailsScreen, PlayerScreen',
     'ScheduleScreen, AuthScreen, SplashScreen',
     'TV Material3 компоненты (D-pad фокус)'],
    fc='#D6E4F0', title_fs=9.5)

box(3.2, 5.5, 5.0, 1.4,
    'ViewModel Layer — Business Logic',
    ['HomeViewModel, DetailsViewModel',
     'PlayerViewModel (StateFlow / viewModelScope)',
     'Обработка событий пульта ДУ'],
    fc='#D5E8D4', title_fs=9.5)

box(3.2, 3.5, 5.0, 1.4,
    'Data Layer — Repository',
    ['MatchRepository (интерфейс)',
     'ApiMatchRepository (Retrofit2 + OkHttp)',
     'MockMatchRepository (тесты)'],
    fc='#FFF2CC', title_fs=9.5)

box(3.2, 1.7, 5.0, 0.9,
    'Локальный кэш / SharedPreferences',
    ['JWT-токен · кэш изображений Coil'],
    fc='#FADBD8', title_fs=9, item_fs=8)

# Стрелки внутри клиента
arrow(3.2, 6.77, 3.2, 6.23, bidirectional=True, label='StateFlow\n/ Events')
arrow(3.2, 4.77, 3.2, 4.23, bidirectional=True, label='Result<T>\n/ suspend')
arrow(3.2, 2.77, 3.2, 2.17, bidirectional=True)

# =================== SERVER SIDE ===================
server_rect = FancyBboxPatch((7.9, 1.0), 5.8, 8.0,
                             boxstyle='round,pad=0.2',
                             facecolor='#EAFAF1', edgecolor='#1E8449',
                             linewidth=2.5, linestyle='--', zorder=1)
ax.add_patch(server_rect)
ax.text(10.8, 9.1, 'Серверная часть (Backend)', ha='center',
        fontsize=9.5, color='#1E8449', fontweight='bold')

box(10.8, 7.5, 5.0, 1.4,
    'REST API Layer',
    ['GET /api/home — каталог трансляций',
     'GET /api/match/{id} — детали матча',
     'POST /auth/send-code, /auth/verify'],
    fc='#D5F5E3', title_fs=9.5)

box(10.8, 5.5, 5.0, 1.4,
    'Business Logic',
    ['Агрегация расписания с sport-tv.by',
     'Генерация и верификация OTP-кодов',
     'Управление подписками пользователей'],
    fc='#A9DFBF', title_fs=9.5)

box(10.8, 3.5, 5.0, 1.4,
    'Data Access Layer',
    ['СУБД (PostgreSQL / расписание)',
     'Кэш Redis (расписание, токены)',
     'Scraper (HLS-ссылки трансляций)'],
    fc='#FDEBD0', title_fs=9.5)

box(10.8, 1.7, 5.0, 0.9,
    'Внешние источники данных',
    ['sport-tv.by/list.php · CDN видеопотоков'],
    fc='#F9EBEA', title_fs=9, item_fs=8)

arrow(10.8, 6.77, 10.8, 6.23, bidirectional=True, label='DTO / бизнес-объекты')
arrow(10.8, 4.77, 10.8, 4.23, bidirectional=True, label='SQL / Redis')
arrow(10.8, 2.77, 10.8, 2.17, bidirectional=True)

# =================== CENTER CONNECTOR ===================
# HTTP/HTTPS стрелки
arrow(6.2, 7.5, 7.9, 7.5, label='HTTP/HTTPS\n(JSON)', bidirectional=True, lw=2.2)
arrow(6.2, 3.5, 7.9, 3.5, label='JWT Auth', bidirectional=True, lw=1.6)

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/07_architecture.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('Architecture diagram saved → 07_architecture.png')
