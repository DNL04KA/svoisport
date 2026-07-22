package com.svoysport.tv.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.MatchCard
import com.svoysport.tv.ui.screens.auth.TvKeyboard
import com.svoysport.tv.ui.theme.Background
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Gray4

/**
 * Поиск: поле и клавиатура по центру, результаты появляются очередью
 * (горизонтальным рядом карточек) под клавиатурой.
 */
@Composable
fun SearchContent(
    onMatchClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isRussian by remember { mutableStateOf(false) }
    var isUpper   by remember { mutableStateOf(false) }
    // Фильтр доступа (Figma: радио «Бесплатные» / «По подписке»); null — все
    var paidFilter by remember { mutableStateOf<Boolean?>(null) }

    // Фокус сразу на клавиатуру — чтобы сайдбар-оверлей свернулся и контент был виден
    val kbFr = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        runCatching { kbFr.requestFocus() }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Background)) {
        val sw = maxWidth.value
        val sh = maxHeight.value
        val scale = minOf(sw / 1920f, sh / 1080f, 1f).coerceAtLeast(0.35f)
        val pad: Dp = (40f * scale).dp

        Column(
            modifier = Modifier.fillMaxSize().padding(pad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = "Поиск",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (40f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height((18f * scale).dp))

            // ── Поле ввода (по центру, синяя рамка — Figma Final UI) ─────────
            Box(
                modifier = Modifier.fillMaxWidth(0.62f).height((70f * scale).dp)
                    .background(Color(0xFF343B4B).copy(alpha = 0.5f), RoundedCornerShape((14f * scale).dp))
                    .border(2.dp, Color(0xFF4556EB), RoundedCornerShape((14f * scale).dp))
                    .padding(horizontal = (20f * scale).dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((12f * scale).dp)) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                        contentDescription = null, tint = Gray3,
                        modifier = Modifier.size((24f * scale).dp)
                    )
                    Text(
                        text  = state.query.ifEmpty { "Введите название команды или турнира" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (22f * scale).sp,
                            color = if (state.query.isEmpty()) Color.White.copy(alpha = 0.35f) else Color(0xFFE2E2E2)
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height((16f * scale).dp))

            // ── Радио-фильтры доступа (Figma: Бесплатные / По подписке) ──────
            Row(horizontalArrangement = Arrangement.spacedBy((28f * scale).dp)) {
                FilterRadio(
                    label    = "Бесплатные",
                    selected = paidFilter == false,
                    scale    = scale,
                    onClick  = { paidFilter = if (paidFilter == false) null else false }
                )
                FilterRadio(
                    label    = "По подписке",
                    selected = paidFilter == true,
                    scale    = scale,
                    onClick  = { paidFilter = if (paidFilter == true) null else true }
                )
            }

            Spacer(Modifier.height((18f * scale).dp))

            // ── Клавиатура (по центру) ───────────────────────────────────────
            TvKeyboard(
                isRussian   = isRussian,
                isUpperCase = isUpper,
                firstKeyFr  = kbFr,
                keySize     = (58f * scale).dp,
                keyGap      = (11f * scale).dp,
                blockGap    = (24f * scale).dp,
                onChar      = { c -> viewModel.onQueryChange(state.query + if (isUpper) c.uppercase() else c.lowercase()) },
                onDelete    = { if (state.query.isNotEmpty()) viewModel.onQueryChange(state.query.dropLast(1)) },
                onClear     = { viewModel.onQueryChange("") },
                onToggleLang = { isRussian = !isRussian },
                onToggleCase = { isUpper = !isUpper }
            )

            Spacer(Modifier.height((22f * scale).dp))

            // ── «Результаты поиска» + очередь карточек под клавиатурой ───────
            val results = remember(state.results, paidFilter) {
                when (paidFilter) {
                    null -> state.results
                    else -> state.results.filter { it.isSubscriptionRequired == paidFilter }
                }
            }
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text(
                    text  = "Результаты поиска",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (26f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                    )
                )
                Spacer(Modifier.height((14f * scale).dp))
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    when {
                        state.error != null   -> CenterText(state.error!!, scale)
                        state.query.isBlank() -> CenterText("Начните вводить запрос", scale)
                        results.isEmpty()     -> CenterText("Ничего не найдено", scale)
                        else -> LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy((16f * scale).dp),
                            contentPadding = PaddingValues(horizontal = (8f * scale).dp)
                        ) {
                            items(results, key = { it.id }) { match ->
                                MatchCard(match = match, onClick = onMatchClick, scale = scale)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Радио-фильтр (Figma Controls): круг — синий с точкой при выборе, серый контур иначе. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FilterRadio(
    label: String,
    selected: Boolean,
    scale: Float,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick  = onClick,
        modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
        shape    = ClickableSurfaceDefaults.shape(RoundedCornerShape((10f * scale).dp)),
        colors   = ClickableSurfaceDefaults.colors(
            containerColor        = Color.Transparent,
            focusedContainerColor = Color.White.copy(alpha = 0.10f)
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = (10f * scale).dp, vertical = (6f * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((10f * scale).dp)
        ) {
            val ring = (22f * scale).dp
            Box(
                modifier = Modifier.size(ring).border(
                    2.dp,
                    if (selected) Color(0xFF4556EB) else Color(0xFF65666E),
                    RoundedCornerShape(50)
                ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        Modifier.size(ring * 0.5f)
                            .background(Color(0xFF4556EB), RoundedCornerShape(50))
                    )
                }
            }
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = (20f * scale).sp,
                    color = if (isFocused || selected) Color.White else Color(0xFFA8A9B2)
                )
            )
        }
    }
}

@Composable
private fun CenterText(text: String, scale: Float) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text  = text,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = (20f * scale).sp, color = Gray4)
        )
    }
}
