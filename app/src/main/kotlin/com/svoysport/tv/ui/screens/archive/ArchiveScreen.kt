package com.svoysport.tv.ui.screens.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.*

// ─── Data models ─────────────────────────────────────────────────────────────

data class ArchiveMatch(
    val id: String,
    val title: String,
    val competition: String,
    val date: String,
    val duration: String,
    val thumbnailUrl: String,
    val isSubscriptionRequired: Boolean = false
)

data class ArchiveDateGroup(
    val date: String,
    val matches: List<ArchiveMatch>
)

// ─── Mock data ────────────────────────────────────────────────────────────────

private val footballUrl   = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?q=80&w=600"
private val hockeyUrl     = "https://images.unsplash.com/photo-1515703407324-5f753afd8be8?q=80&w=600"
private val basketballUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?q=80&w=600"
private val volleyballUrl = "https://images.unsplash.com/photo-1612872087720-bb876e2e67d1?q=80&w=600"

private val mockGroups = listOf(
    ArchiveDateGroup("20.11.2025", listOf(
        ArchiveMatch("a01", "Зенит — Спартак",            "РПЛ • Футбол",            "20.11.2025", "1:42:30", footballUrl),
        ArchiveMatch("a02", "Реал Мадрид — Барселона",    "Ла Лига • Футбол",        "20.11.2025", "1:58:10", footballUrl, true),
        ArchiveMatch("a03", "ЦСКА — Авангард",            "КХЛ • Хоккей",            "20.11.2025", "2:14:05", hockeyUrl),
        ArchiveMatch("a04", "СКА — Металлург",            "КХЛ • Хоккей",            "20.11.2025", "1:50:22", hockeyUrl, true),
        ArchiveMatch("a05", "ЦСКА — Химки",               "Единая лига • Баскетбол", "20.11.2025", "1:38:40", basketballUrl)
    )),
    ArchiveDateGroup("19.11.2025", listOf(
        ArchiveMatch("a06", "Зенит-Казань — Белогорье",   "Суперлига • Волейбол",    "19.11.2025", "1:22:15", volleyballUrl),
        ArchiveMatch("a07", "Манчестер Сити — Ливерпуль", "АПЛ • Футбол",            "19.11.2025", "2:01:55", footballUrl, true),
        ArchiveMatch("a08", "Ак Барс — Динамо Минск",     "КХЛ • Хоккей",            "19.11.2025", "1:55:30", hockeyUrl),
        ArchiveMatch("a09", "УНИКС — Локомотив-Кубань",   "Единая лига • Баскетбол", "19.11.2025", "1:40:00", basketballUrl, true)
    )),
    ArchiveDateGroup("18.11.2025", listOf(
        ArchiveMatch("a10", "Факел — Кузбасс",            "Суперлига • Волейбол",    "18.11.2025", "1:28:50", volleyballUrl),
        ArchiveMatch("a11", "Краснодар — Локомотив",      "РПЛ • Футбол",            "18.11.2025", "1:46:20", footballUrl),
        ArchiveMatch("a12", "Динамо Мск — УГМК",          "Еврокубок • Баскетбол",   "18.11.2025", "1:35:45", basketballUrl, true),
        ArchiveMatch("a13", "Салават Юлаев — Авангард",   "КХЛ Плей-офф • Хоккей",  "18.11.2025", "2:30:10", hockeyUrl)
    ))
)

// ─── ArchiveScreen ────────────────────────────────────────────────────────────
// Figma 614:23155 — Cards 400×237dp r=20 gap=20, date fs=28 fw=500

@Composable
fun ArchiveScreen(
    onMatchClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1760f, sh / 1080f, 1f).coerceAtLeast(0.35f)

        val cardW   : Dp       = (400f * scale).dp
        val cardH   : Dp       = (237f * scale).dp
        val cardGap : Dp       = (20f  * scale).dp
        val dateFs  : TextUnit = (28f  * scale).coerceAtLeast(12f).sp
        val groupGap: Dp       = (32f  * scale).dp
        val pad     : Dp       = (24f  * scale).dp

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(horizontal = pad, vertical = pad),
            verticalArrangement = Arrangement.spacedBy(groupGap)
        ) {
            items(mockGroups, key = { it.date }) { group ->
                ArchiveDateGroupRow(
                    group    = group,
                    cardW    = cardW,
                    cardH    = cardH,
                    cardGap  = cardGap,
                    dateFs   = dateFs,
                    innerGap = (24f * scale).dp,
                    onMatchClick = onMatchClick
                )
            }
        }
    }
}

// ─── ArchiveDateGroupRow ──────────────────────────────────────────────────────

@Composable
private fun ArchiveDateGroupRow(
    group   : ArchiveDateGroup,
    cardW   : Dp,
    cardH   : Dp,
    cardGap : Dp,
    dateFs  : TextUnit,
    innerGap: Dp,
    onMatchClick: (String) -> Unit
) {
    Column(
        modifier            = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(innerGap)
    ) {
        Text(
            text  = group.date,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize   = dateFs,
                fontWeight = FontWeight.Medium,
                color      = Color(0xFFE2E2E2)
            )
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(cardGap)) {
            items(group.matches, key = { it.id }) { match ->
                ArchiveCard(match = match, cardW = cardW, cardH = cardH, onMatchClick = onMatchClick)
            }
        }
    }
}

// ─── ArchiveCard ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ArchiveCard(
    match: ArchiveMatch,
    cardW: Dp,
    cardH: Dp,
    onMatchClick: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val sc by animateFloatAsState(if (isFocused) 1.08f else 1f, tween(150), label = "ac")
    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        onClick  = { onMatchClick(match.id) },
        modifier = Modifier.width(cardW).height(cardH)
            .onFocusChanged { isFocused = it.isFocused }.scale(sc),
        shape  = ClickableSurfaceDefaults.shape(cardShape),
        colors = ClickableSurfaceDefaults.colors(containerColor = CardBg, focusedContainerColor = CardBg),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(border = androidx.compose.foundation.BorderStroke(2.dp, Primary), shape = cardShape)
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = match.thumbnailUrl, contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(cardShape),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(cardH * 0.42f).align(Alignment.BottomStart)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f))))
            )

            if (match.isSubscriptionRequired) {
                Box(
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        .clip(RoundedCornerShape(8.dp)).background(Primary.copy(alpha = 0.90f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text  = "Подписка",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold, fontSize = (cardH.value * 0.084f).sp, color = Color.White
                        )
                    )
                }
            }

            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                    .clip(RoundedCornerShape(6.dp)).background(Color.Black.copy(alpha = 0.70f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text  = match.duration,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = (cardH.value * 0.084f).sp, color = Color.White
                    )
                )
            }

            AnimatedVisibility(
                visible = isFocused, enter = fadeIn(tween(120)), exit = fadeOut(tween(100)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier         = Modifier.size(cardH * 0.22f).background(Primary.copy(alpha = 0.92f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = ImageVector.vectorResource(R.drawable.ic_play),
                        contentDescription = "Смотреть", tint = Color.White,
                        modifier           = Modifier.size(cardH * 0.11f).offset(x = 1.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 14.dp, end = 50.dp)
            ) {
                Text(
                    text     = match.title,
                    style    = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold, fontSize = (cardH.value * 0.093f).sp, color = Color.White
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text     = match.competition,
                    style    = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (cardH.value * 0.076f).sp, color = Color(0xFFA8A9B2)
                    ),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
