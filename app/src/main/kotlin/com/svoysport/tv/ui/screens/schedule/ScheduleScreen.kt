package com.svoysport.tv.ui.screens.schedule

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.*

// ─── Data models ─────────────────────────────────────────────────────────────

data class ScheduleDay(
    val id: Int,
    val shortName: String,
    val fullName: String,
    val date: String,
    val fullDate: String,
    val isToday: Boolean = false,
    val startMs: Long = 0L
)

enum class MatchStatus { UPCOMING, LIVE, FINISHED }

data class ScheduleMatch(
    val id: String,
    val time: String,
    val title: String,
    val competition: String,
    val sport: SportFilter,
    val status: MatchStatus = MatchStatus.UPCOMING,
    val isSubscriptionRequired: Boolean = false
)

enum class SportFilter(val label: String) {
    ALL("Все"), FOOTBALL("Футбол"), HOCKEY("Хоккей"),
    BASKETBALL("Баскетбол"), VOLLEYBALL("Волейбол"), HANDBALL("Гандбол")
}

// ─── Mock data ───────────────────────────────────────────────────────────────

private val mockDays = listOf(
    ScheduleDay(0, "Пн", "Понедельник",  "21 ноя", "21 ноября 2025"),
    ScheduleDay(1, "Вт", "Вторник",      "22 ноя", "22 ноября 2025"),
    ScheduleDay(2, "Ср", "Среда",        "23 ноя", "23 ноября 2025", isToday = true),
    ScheduleDay(3, "Чт", "Четверг",      "24 ноя", "24 ноября 2025"),
    ScheduleDay(4, "Пт", "Пятница",      "25 ноя", "25 ноября 2025"),
    ScheduleDay(5, "Сб", "Суббота",      "26 ноя", "26 ноября 2025"),
    ScheduleDay(6, "Вс", "Воскресенье",  "27 ноя", "27 ноября 2025")
)

private val mockMatchesByDay: Map<Int, List<ScheduleMatch>> = mapOf(
    0 to listOf(
        ScheduleMatch("m0_1","10:00","Металлург — Ак Барс","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m0_2","12:30","ЦСКА — Спартак","РПЛ • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true),
        ScheduleMatch("m0_3","15:00","Зенит — Локомотив","РПЛ • Футбол",SportFilter.FOOTBALL),
        ScheduleMatch("m0_4","17:30","Динамо — Уфа","КХЛ • Хоккей",SportFilter.HOCKEY,isSubscriptionRequired=true),
        ScheduleMatch("m0_5","19:45","Краснодар — Сочи","РПЛ • Футбол",SportFilter.FOOTBALL)
    ),
    1 to listOf(
        ScheduleMatch("m1_1","11:00","Авангард — СКА","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m1_2","13:00","БК Нижний — ЦСКА","МХЛ • Баскетбол",SportFilter.BASKETBALL,isSubscriptionRequired=true),
        ScheduleMatch("m1_3","15:30","Химки — Локомотив-Кубань","МХЛ • Баскетбол",SportFilter.BASKETBALL),
        ScheduleMatch("m1_4","18:00","Трактор — Витязь","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m1_5","20:00","Зенит-Казань — Белогорье","Суперлига • Волейбол",SportFilter.VOLLEYBALL,isSubscriptionRequired=true)
    ),
    2 to listOf(
        ScheduleMatch("m2_0","11:00","Батаво Маккензи — Флуминенсе","Чемп. Бразилии • Волейбол",SportFilter.VOLLEYBALL,MatchStatus.FINISHED),
        ScheduleMatch("m2_1","11:30","Рубин — Молот","МХЛ • Хоккей",SportFilter.HOCKEY,MatchStatus.LIVE),
        ScheduleMatch("m2_2","11:30","НН-Мещерский — БК Минск-м","МХЛ • Баскетбол",SportFilter.BASKETBALL,MatchStatus.LIVE),
        ScheduleMatch("m2_3","11:50","Снеж. Барсы — Куз. Медведи","Футбол, Чем. 1 лига",SportFilter.FOOTBALL),
        ScheduleMatch("m2_4","12:00","Днепр-Могилев — Орша","МХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m2_5","12:20","Снеж. Барсы — Куз. Медведи","МХЛ • Хоккей",SportFilter.HOCKEY,isSubscriptionRequired=true),
        ScheduleMatch("m2_6","12:20","Батаво Маккензи — Флуминенсе","Чемп. Бразилии • Волейбол • Мужчины",SportFilter.VOLLEYBALL),
        ScheduleMatch("m2_7","12:30","Батаво Маккензи — Флуминенсе","Чемп. Бразилии • Волейбол • Мужчины",SportFilter.VOLLEYBALL),
        ScheduleMatch("m2_8","12:40","Снеж. Барсы — Куз. Медведи","МХЛ • Хоккей",SportFilter.HOCKEY)
    ),
    3 to listOf(
        ScheduleMatch("m3_1","12:00","Реал Мадрид — Барселона","Ла Лига • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true),
        ScheduleMatch("m3_2","14:30","УНИКС — Зенит-СПб","МХЛ • Баскетбол",SportFilter.BASKETBALL),
        ScheduleMatch("m3_3","17:00","Торпедо — Куньлунь","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m3_4","19:30","Факел — Белогорье","Суперлига • Волейбол",SportFilter.VOLLEYBALL,isSubscriptionRequired=true)
    ),
    4 to listOf(
        ScheduleMatch("m4_1","11:00","Манчестер Сити — Ливерпуль","АПЛ • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true),
        ScheduleMatch("m4_2","13:30","Авангард — Барыс","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m4_3","16:00","Локомотив-Кубань — Парма","Суперлига • Волейбол",SportFilter.VOLLEYBALL),
        ScheduleMatch("m4_4","20:00","Реал — Атлетико","Ла Лига • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true)
    ),
    5 to listOf(
        ScheduleMatch("m5_1","10:00","Салават Юлаев — Металлург","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m5_2","12:30","Динамо — Локомотив","РПЛ • Футбол",SportFilter.FOOTBALL),
        ScheduleMatch("m5_3","15:00","ЦСКА — Химки","МХЛ • Баскетбол",SportFilter.BASKETBALL,isSubscriptionRequired=true),
        ScheduleMatch("m5_4","17:30","ПСЖ — Марсель","Лига 1 • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true),
        ScheduleMatch("m5_5","20:00","Зенит-Казань — Кузбасс","Суперлига • Волейбол",SportFilter.VOLLEYBALL)
    ),
    6 to listOf(
        ScheduleMatch("m6_1","11:00","Атлетик — Реал Сосьедад","Ла Лига • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true),
        ScheduleMatch("m6_2","13:30","СКА — Локомотив (х)","КХЛ • Хоккей",SportFilter.HOCKEY),
        ScheduleMatch("m6_3","16:00","ЦСКА — Автодор","МХЛ • Баскетбол",SportFilter.BASKETBALL),
        ScheduleMatch("m6_4","18:30","Манчестер Юнайтед — Тоттенхэм","АПЛ • Футбол",SportFilter.FOOTBALL,isSubscriptionRequired=true)
    )
)

// ─── ScheduleScreen ──────────────────────────────────────────────────────────
// Figma 617:26993 — 1920×1080, Days 420dp + gap 20dp + Matches 900dp
// Адаптация: пропорции сохранены через weight, высоты через scale

@Composable
fun ScheduleScreen(
    onMatchClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val dayLoaded = state.matchesByDay.containsKey(state.selectedDayId)
    val matches = state.matchesByDay[state.selectedDayId] ?: emptyList()

    // Матч в фокусе — для боковой кнопки «В избранное» (выравнивается по строке)
    var focusedMatch by remember { mutableStateOf<ScheduleMatch?>(null) }
    var focusedRowY  by remember { mutableStateOf(0f) }
    val favIds = com.svoysport.tv.session.FavoritesManager.favoriteIds.value

    // Снекбар
    var snackText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(snackText) {
        if (snackText != null) { kotlinx.coroutines.delay(2200); snackText = null }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1760f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val panelGap  : Dp = (20f  * scale).dp
        val panelH    : Dp = (698f * scale).dp.coerceAtMost(sh.dp * 0.88f)
        val rowH      : Dp = (90f  * scale).dp
        val fontSize  : TextUnit = (26f * scale).sp
        val fontSizeSm: TextUnit = (22f * scale).sp

        if (state.days.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Загрузка расписания…", style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = fontSize, color = Color.White.copy(alpha = 0.6f)))
            }
        } else {
            Row(
                modifier              = Modifier.fillMaxSize().padding((20f * scale).dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                DaysPanel(
                    days          = state.days,
                    selectedDayId = state.selectedDayId,
                    panelH        = panelH,
                    fontSize      = fontSize,
                    fontSizeSm    = fontSizeSm,
                    onDaySelected = { viewModel.selectDay(it) },
                    modifier      = Modifier.weight(360f)
                )

                Spacer(Modifier.width(panelGap))

                MatchListPanel(
                    matches        = matches,
                    favIds         = favIds,
                    activeId       = focusedMatch?.id,
                    isLoading      = !dayLoaded && state.isLoading,
                    error          = state.error,
                    panelH         = panelH,
                    rowH           = rowH,
                    fontSize       = fontSize,
                    fontSizeSm     = fontSizeSm,
                    onMatchClick   = onMatchClick,
                    onMatchFocused = { m, y -> focusedMatch = m; focusedRowY = y },
                    modifier       = Modifier.weight(760f)
                )

                Spacer(Modifier.width(panelGap))

                // Боковая кнопка «В избранное» — напротив строки, на которую наведён фокус
                FavoriteAction(
                    match    = focusedMatch,
                    isFav    = focusedMatch?.id in favIds,
                    scale    = scale,
                    rowY     = focusedRowY,
                    modifier = Modifier.weight(280f),
                    onToggle = { m ->
                        com.svoysport.tv.session.FavoritesManager.toggle(m.id)
                        snackText = if (m.id in com.svoysport.tv.session.FavoritesManager.favoriteIds.value)
                            "Трансляция добавлена в Избранное" else "Удалено из Избранного"
                    }
                )
            }
        }

        // ── Снекбар (нижний центр) ───────────────────────────────────────────
        snackText?.let { text ->
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding((24f * scale).dp)
                    .clip(RoundedCornerShape((14f * scale).dp))
                    .background(Color(0xFFE2E2E2))
                    .padding(horizontal = (20f * scale).dp, vertical = (14f * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_bookmark_active),
                        contentDescription = null, tint = Primary,
                        modifier = Modifier.size((20f * scale).dp)
                    )
                    Text(
                        text  = text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (20f * scale).sp, color = Color(0xFF171717)
                        )
                    )
                }
            }
        }
    }
}

// ─── FavoriteAction (боковая кнопка) ──────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FavoriteAction(
    match: ScheduleMatch?,
    isFav: Boolean,
    scale: Float,
    rowY: Float,
    modifier: Modifier = Modifier,
    onToggle: (ScheduleMatch) -> Unit
) {
    if (match == null) {
        Box(modifier) {}
        return
    }
    var isFocused by remember { mutableStateOf(false) }
    var colY by remember { mutableStateOf(0f) }
    Box(
        modifier = modifier.onGloballyPositioned { colY = it.positionInRoot().y },
        contentAlignment = Alignment.TopStart
    ) {
        Surface(
            onClick  = { onToggle(match) },
            modifier = Modifier.fillMaxWidth().height((72f * scale).dp)
                .offset { IntOffset(0, (rowY - colY).coerceAtLeast(0f).toInt()) }
                .onFocusChanged { isFocused = it.isFocused },
            shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape((16f * scale).dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor        = Color.White.copy(alpha = 0.06f),
                focusedContainerColor = Color.White.copy(alpha = 0.10f)
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                    shape  = RoundedCornerShape((16f * scale).dp)
                )
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = (18f * scale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        if (isFav) R.drawable.ic_bookmark_active else R.drawable.ic_bookmark
                    ),
                    contentDescription = null,
                    tint = if (isFav) Primary else Color.White,
                    modifier = Modifier.size((24f * scale).dp)
                )
                Text(
                    text  = if (isFav) "В избранном" else "В избранное",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium, fontSize = (22f * scale).sp, color = Color.White
                    )
                )
            }
        }
    }
}

// ─── DaysPanel ───────────────────────────────────────────────────────────────

@Composable
private fun DaysPanel(
    days: List<ScheduleDay>,
    selectedDayId: Int,
    panelH: Dp,
    fontSize: TextUnit,
    fontSizeSm: TextUnit,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(panelH)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(10.dp)
    ) {
        // Все 7 дней делят высоту панели поровну через weight — Воскресенье
        // больше не обрезается на нижней кромке независимо от размера экрана.
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                DayItem(
                    day        = day,
                    isSelected = day.id == selectedDayId,
                    fontSize   = fontSize,
                    fontSizeSm = fontSizeSm,
                    onClick    = { onDaySelected(day.id) },
                    modifier   = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─── DayItem ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DayItem(
    day: ScheduleDay,
    isSelected: Boolean,
    fontSize: TextUnit,
    fontSizeSm: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val isActive  = isSelected || isFocused

    Surface(
        onClick  = onClick,
        modifier = modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        // Figma Days: focused — светлая плашка #E2E2E2 с тёмным текстом и синей рамкой,
        // selected — серый чип с белым текстом
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = if (isSelected) Color.White.copy(alpha = 0.18f) else Color.Transparent,
            focusedContainerColor = Color(0xFFE2E2E2)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape  = RoundedCornerShape(20.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.0f)
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val dayLabel = if (day.isToday) "${day.fullName} • Сегодня" else day.fullName
                Text(
                    text     = dayLabel,
                    style    = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize   = fontSize,
                        color      = when {
                            isFocused  -> Color(0xFF65666E)
                            isSelected -> Color.White
                            else       -> Color.White.copy(alpha = 0.60f)
                        }
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = day.fullDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSizeSm,
                        color    = when {
                            isFocused  -> Color(0xFF404147)
                            isSelected -> Color(0xFFA8A9B2)
                            else       -> Color.White.copy(alpha = 0.40f)
                        }
                    )
                )
            }
            if (isActive) {
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint               = if (isFocused) Color(0xFF404147) else Color.White,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── MatchListPanel ──────────────────────────────────────────────────────────

@Composable
private fun MatchListPanel(
    matches: List<ScheduleMatch>,
    favIds: Set<String> = emptySet(),
    activeId: String? = null,
    isLoading: Boolean,
    error: String?,
    panelH: Dp,
    rowH: Dp,
    fontSize: TextUnit,
    fontSizeSm: TextUnit,
    onMatchClick: (String) -> Unit,
    onMatchFocused: (ScheduleMatch, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(panelH)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(10.dp)
    ) {
        if (isLoading || error != null || matches.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text  = when {
                        isLoading    -> "Загрузка…"
                        error != null -> error
                        else         -> "На этот день трансляций нет"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = fontSize, color = Color.White.copy(alpha = 0.50f)
                    )
                )
            }
        } else {
            LazyColumn(
                state               = rememberLazyListState(),
                modifier            = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(matches, key = { it.id }) { match ->
                    ScheduleMatchRow(
                        match        = match,
                        isFav        = match.id in favIds,
                        isActive     = match.id == activeId,
                        rowH         = rowH,
                        fontSize     = fontSize,
                        fontSizeSm   = fontSizeSm,
                        onMatchClick = onMatchClick,
                        onFocused    = onMatchFocused
                    )
                }
            }
        }
    }
}

// ─── ScheduleMatchRow ────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ScheduleMatchRow(
    match: ScheduleMatch,
    isFav: Boolean = false,
    isActive: Boolean = false,
    rowH: Dp,
    fontSize: TextUnit,
    fontSizeSm: TextUnit,
    onMatchClick: (String) -> Unit,
    onFocused: (ScheduleMatch, Float) -> Unit = { _, _ -> }
) {
    var isFocused by remember { mutableStateOf(false) }
    var rowWinY by remember { mutableStateOf(0f) }

    Surface(
        onClick  = { onMatchClick(match.id) },
        modifier = Modifier.fillMaxWidth().height(rowH)
            .onGloballyPositioned { rowWinY = it.positionInRoot().y }
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocused(match, rowWinY)
            },
        shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
        // Figma: строка в фокусе — тёмная, чуть светлее остальных, с синей
        // рамкой; слегка подсвеченной остаётся и пока фокус на «В избранное»
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = when {
                isActive                             -> Color.White.copy(alpha = 0.10f)
                match.status == MatchStatus.FINISHED -> Color.White.copy(alpha = 0.04f)
                else                                 -> Color.White.copy(alpha = 0.08f)
            },
            focusedContainerColor = Color.White.copy(alpha = 0.10f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape  = RoundedCornerShape(20.dp)
            )
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Row(
            modifier          = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Время + бейдж «Live» под ним (Figma) ─────────────────────────
            Column(
                modifier            = Modifier.width(rowH * 0.95f),
                verticalArrangement = Arrangement.spacedBy((4f).dp)
            ) {
                Text(
                    text  = match.time,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = fontSize,
                        color = if (match.status == MatchStatus.FINISHED) Color.White.copy(alpha = 0.35f) else Color.White
                    )
                )
                if (match.status == MatchStatus.LIVE) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(LiveRed)
                            .padding(horizontal = 9.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text  = "Live",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = (fontSizeSm.value * 0.78f).sp,
                                color      = Color.White
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = match.title,
                    style    = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = fontSize,
                        color = if (match.status == MatchStatus.FINISHED) Color.White.copy(alpha = 0.40f) else Color.White
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = match.competition,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        fontSize = fontSizeSm,
                        color    = if (match.status == MatchStatus.FINISHED) Color.White.copy(alpha = 0.30f) else Color(0xFFA8A9B2)
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            if (match.isSubscriptionRequired) {
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(70.dp))
                        .background(Primary)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Подписка", style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = fontSizeSm, color = Color.White))
                }
            }
            // Закладка у избранной трансляции (Figma: строка 12:00)
            if (isFav && !isFocused) {
                Spacer(Modifier.width(14.dp))
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_bookmark_active),
                    contentDescription = "В избранном",
                    tint               = Color.White.copy(alpha = 0.90f),
                    modifier           = Modifier.size(20.dp)
                )
            }
            // Шеврон появляется на фокусе — как в Figma
            if (isFocused) {
                Spacer(Modifier.width(14.dp))
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
    }
}
