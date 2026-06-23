"""MVVM + StateFlow — схема потока данных (для раздела 5.1 / 5.2)"""
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch, FancyArrowPatch

fig, ax = plt.subplots(figsize=(16, 9))
ax.set_xlim(0, 16)
ax.set_ylim(0, 9)
ax.axis('off')
fig.patch.set_facecolor('white')

# ---- Цветовая схема ----
C_UI      = '#D6E4F0'   # голубой  — UI Layer
C_VM      = '#D5E8D4'   # зелёный  — ViewModel
C_REPO    = '#FFF2CC'   # жёлтый   — Repository
C_DATA    = '#F8D7DA'   # красный  — Data sources
BORDER    = '#1A3A5C'
TXT       = '#1A1A1A'

def box(cx, cy, w, h, label, sublabel='', fc='#E8EEF4', fs=10):
    rect = FancyBboxPatch((cx - w/2, cy - h/2), w, h,
                          boxstyle='round,pad=0.15',
                          facecolor=fc, edgecolor=BORDER, linewidth=1.8, zorder=3)
    ax.add_patch(rect)
    if sublabel:
        ax.text(cx, cy + 0.22, label, ha='center', va='center',
                fontsize=fs, fontweight='bold', color=TXT, zorder=5)
        ax.text(cx, cy - 0.28, sublabel, ha='center', va='center',
                fontsize=7.5, color='#555555', style='italic', zorder=5)
    else:
        ax.text(cx, cy, label, ha='center', va='center',
                fontsize=fs, fontweight='bold', color=TXT, zorder=5)

def arrow(x1, y1, x2, y2, label='', color=BORDER, rad=0.0, lw=1.6, ls='-'):
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle='->', color=color, lw=lw,
                                linestyle=ls,
                                connectionstyle=f'arc3,rad={rad}'))
    if label:
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx, my + 0.18, label, ha='center', va='center',
                fontsize=7.5, color='#333333',
                bbox=dict(boxstyle='round,pad=0.12', facecolor='white',
                          edgecolor='none', alpha=0.9))

def layer_bg(y_bottom, y_top, fc, label, lx=0.25):
    rect = plt.Rectangle((0.15, y_bottom), 15.7, y_top - y_bottom,
                          facecolor=fc, edgecolor='#AAAAAA',
                          linewidth=1.0, linestyle='--',
                          zorder=1, alpha=0.35)
    ax.add_patch(rect)
    ax.text(lx, (y_bottom + y_top) / 2, label, ha='left', va='center',
            fontsize=8, color='#555555', rotation=90, style='italic')

# =================== LAYER BACKGROUNDS ===================
layer_bg(6.3, 8.8, '#CCE5FF', 'UI Layer')
layer_bg(3.5, 6.1, '#D5F5E3', 'ViewModel Layer')
layer_bg(0.3, 3.3, '#FEF9E7', 'Data Layer')

# =================== TITLE ===================
ax.text(8.0, 8.55, 'Схема MVVM с однонаправленным потоком данных (StateFlow)',
        ha='center', va='center', fontsize=12, fontweight='bold', color=TXT)

# =================== UI LAYER ===================
# Три composable-экрана
for i, (name, sub) in enumerate([
    ('HomeScreen', 'collectAsState()'),
    ('DetailsScreen', 'collectAsState()'),
    ('PlayerScreen', 'collectAsState()'),
]):
    cx = 3.2 + i * 3.9
    box(cx, 7.6, 3.2, 0.9, name, sub, fc=C_UI, fs=9)

# =================== VIEWMODEL LAYER ===================
for i, (name, sub) in enumerate([
    ('HomeViewModel', 'MutableStateFlow\n<HomeUiState>'),
    ('DetailsViewModel', 'MutableStateFlow\n<DetailsUiState>'),
    ('PlayerViewModel', 'MutableStateFlow\n<PlayerUiState>'),
]):
    cx = 3.2 + i * 3.9
    box(cx, 4.85, 3.2, 1.5, name, sub, fc=C_VM, fs=9)

# =================== DATA LAYER ===================
box(3.5, 1.85, 3.4, 1.5,  'MatchRepository\n(interface)', '', fc=C_REPO, fs=9)
box(7.6, 1.85, 3.4, 1.5,  'ApiMatchRepository\n(Retrofit2 + OkHttp)', '', fc=C_DATA, fs=9)
box(11.7, 1.85, 2.8, 1.5, 'MockMatchRepository\n(тестовые данные)', '', fc='#EDE7F6', fs=9)

# =================== ARROWS ===================
# UI → ViewModel: events
for i in range(3):
    cx = 3.2 + i * 3.9
    arrow(cx - 0.4, 7.14, cx - 0.4, 6.62, color='#2471A3', lw=1.4, rad=0.0)

# ViewModel → UI: state (StateFlow)
for i in range(3):
    cx = 3.2 + i * 3.9
    arrow(cx + 0.4, 6.62, cx + 0.4, 7.14, label='StateFlow\n(uiState)', color='#1E8449', lw=1.6)

# ViewModel → Repository
for i in range(3):
    cx = 3.2 + i * 3.9
    arrow(cx, 4.10, 3.5 if i == 0 else (7.6 if i == 1 else 11.7),
          2.63, color='#7D6608', lw=1.3, ls='--')

# Repository → ApiMatchRepository
arrow(5.2, 1.85, 5.9, 1.85, label='implement', color='#922B21', lw=1.4)
# Repository → MockMatchRepository
arrow(5.2, 1.85, 10.3, 1.85, label='implement (test)', color='#6C3483', lw=1.2, ls='--')

# ApiMatchRepository → external
ax.annotate('', xy=(9.3, 0.65), xytext=(8.5, 1.10),
            arrowprops=dict(arrowstyle='->', color='#922B21', lw=1.4))
box(10.5, 0.55, 3.0, 0.65, 'sport-tv.by API\n(JSON / HTTP)', fc='#FADBD8', fs=8)

# =================== LEGEND ===================
lx, ly = 0.4, 2.9
items = [
    (C_UI,    'UI-экраны (Composable)'),
    (C_VM,    'ViewModel (бизнес-логика)'),
    (C_REPO,  'Repository (интерфейс)'),
    (C_DATA,  'Источники данных'),
]
ax.text(lx, ly + 0.3, 'Условные обозначения:', fontsize=8, fontweight='bold')
for j, (fc, lbl) in enumerate(items):
    ry = ly - 0.38 * (j + 1)
    rect = FancyBboxPatch((lx, ry - 0.12), 0.4, 0.26,
                          boxstyle='round,pad=0.04',
                          facecolor=fc, edgecolor=BORDER, linewidth=1.0)
    ax.add_patch(rect)
    ax.text(lx + 0.55, ry + 0.01, lbl, fontsize=7.5, va='center')

plt.tight_layout()
plt.savefig('/Users/nesterovichdaniil/Desktop/ProgramOnTV/docs/diagrams/06_mvvm_flow.png',
            dpi=180, bbox_inches='tight', facecolor='white')
print('MVVM flow diagram saved → 06_mvvm_flow.png')
