package com.svoysport.tv.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun SearchContent(
    onMatchClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var isRussian by remember { mutableStateOf(false) }
    var isUpper   by remember { mutableStateOf(false) }

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
        val pad: Dp = (48f * scale).dp

        Row(modifier = Modifier.fillMaxSize().padding(pad)) {

            // ── Левая колонка: поле + клавиатура ─────────────────────────────
            Column(modifier = Modifier.weight(0.46f)) {
                Text(
                    text  = "Поиск",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (40f * scale).sp, fontWeight = FontWeight.SemiBold, color = Color.White
                    )
                )
                Spacer(Modifier.height((20f * scale).dp))

                Box(
                    modifier = Modifier.fillMaxWidth().height((72f * scale).dp)
                        .background(Color(0xFF343B4B).copy(alpha = 0.5f), RoundedCornerShape((14f * scale).dp))
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

                Spacer(Modifier.height((28f * scale).dp))

                TvKeyboard(
                    isRussian   = isRussian,
                    isUpperCase = isUpper,
                    firstKeyFr  = kbFr,
                    keySize     = (62f * scale).dp,
                    keyGap      = (12f * scale).dp,
                    blockGap    = (28f * scale).dp,
                    onChar      = { c -> viewModel.onQueryChange(state.query + if (isUpper) c.uppercase() else c.lowercase()) },
                    onDelete    = { if (state.query.isNotEmpty()) viewModel.onQueryChange(state.query.dropLast(1)) },
                    onClear     = { viewModel.onQueryChange("") },
                    onToggleLang = { isRussian = !isRussian },
                    onToggleCase = { isUpper = !isUpper }
                )
            }

            Spacer(Modifier.width((40f * scale).dp))

            // ── Правая колонка: результаты ───────────────────────────────────
            Box(modifier = Modifier.weight(0.54f).fillMaxHeight()) {
                when {
                    state.error != null -> CenterText(state.error!!, scale)
                    state.query.isBlank() -> CenterText("Начните вводить запрос", scale)
                    state.results.isEmpty() -> CenterText("Ничего не найдено", scale)
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive((240f * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((16f * scale).dp),
                        verticalArrangement   = Arrangement.spacedBy((16f * scale).dp),
                        contentPadding = PaddingValues(bottom = (24f * scale).dp)
                    ) {
                        items(state.results, key = { it.id }) { match ->
                            MatchCard(match = match, onClick = onMatchClick, scale = scale)
                        }
                    }
                }
            }
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
