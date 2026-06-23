package com.svoysport.tv.ui.screens.player

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.LiveBadge
import com.svoysport.tv.ui.theme.*

// ─── PlayerOverlay ────────────────────────────────────────────────────────────

@Composable
fun PlayerOverlay(
    isVisible:      Boolean,
    title:          String,
    competition:    String,
    isLive:         Boolean,
    playbackState:  PlayerPlaybackState,
    onPlayPause:    () -> Unit,
    onSeekForward:  () -> Unit,
    onSeekRewind:   () -> Unit,
    onBack:         () -> Unit,
    modifier:       Modifier = Modifier
) {
    // Фокус на кнопку play/pause каждый раз при появлении оверлея
    val playFr = remember { FocusRequester() }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            runCatching { playFr.requestFocus() }
        }
    }

    AnimatedVisibility(
        visible  = isVisible,
        enter    = fadeIn(tween(200)),
        exit     = fadeOut(tween(300)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.00f to Color.Black.copy(alpha = 0.75f),
                        0.18f to Color.Transparent,
                        0.75f to Color.Transparent,
                        1.00f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        ) {
            // ── Верхняя шапка ────────────────────────────────────────────────
            TopBar(
                title       = title,
                competition = competition,
                isLive      = isLive,
                onBack      = onBack,
                modifier    = Modifier.align(Alignment.TopStart)
            )

            // ── Центральные кнопки управления ────────────────────────────────
            CenterControls(
                isPlaying     = playbackState.isPlaying,
                playFr        = playFr,
                onPlayPause   = onPlayPause,
                onSeekForward = onSeekForward,
                onSeekRewind  = onSeekRewind,
                modifier      = Modifier.align(Alignment.Center)
            )

            // ── Нижняя панель: прогресс + время ──────────────────────────────
            BottomBar(
                isLive        = isLive,
                playbackState = playbackState,
                modifier      = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TopBar(
    title:       String,
    competition: String,
    isLive:      Boolean,
    onBack:      () -> Unit,
    modifier:    Modifier = Modifier
) {
    var isBackFocused by remember { mutableStateOf(false) }
    val backScale by animateFloatAsState(
        if (isBackFocused) 1.08f else 1.0f, tween(150), label = "back"
    )

    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp, vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Кнопка «Назад»
        Surface(
            onClick  = onBack,
            modifier = Modifier
                .onFocusChanged { isBackFocused = it.isFocused }
                .scale(backScale),
            shape  = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor        = Color.White.copy(alpha = 0.12f),
                focusedContainerColor = Color.White.copy(alpha = 0.22f)
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Primary),
                    shape  = RoundedCornerShape(20.dp)
                )
            ),
            scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector        = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text  = "Назад",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color    = Color.White
                    )
                )
            }
        }

        // Разделитель
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )

        // Метаданные матча
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isLive) LiveBadge()
                Text(
                    text  = competition,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        color    = Color.White.copy(alpha = 0.65f)
                    )
                )
            }
            Text(
                text  = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White
                )
            )
        }
    }
}

// ─── CenterControls ───────────────────────────────────────────────────────────

@Composable
private fun CenterControls(
    isPlaying:     Boolean,
    playFr:        FocusRequester,
    onPlayPause:   () -> Unit,
    onSeekForward: () -> Unit,
    onSeekRewind:  () -> Unit,
    modifier:      Modifier = Modifier
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // ← Перемотка назад 10с
        PlayerIconButton(
            iconRes           = R.drawable.ic_replay_10,
            contentDescription = "-10 сек",
            size              = 56.dp,
            onClick           = onSeekRewind
        )

        // ◼ / ▶  Play / Pause  — большая центральная кнопка
        PlayPauseButton(
            isPlaying  = isPlaying,
            focusReq   = playFr,
            onClick    = onPlayPause
        )

        // Перемотка вперёд 10с →
        PlayerIconButton(
            iconRes           = R.drawable.ic_forward_10,
            contentDescription = "+10 сек",
            size              = 56.dp,
            onClick           = onSeekForward
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    focusReq:  FocusRequester,
    onClick:   () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isFocused) 1.12f else 1.0f, tween(150), label = "pp"
    )

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .size(72.dp)
            .focusRequester(focusReq)
            .onFocusChanged { isFocused = it.isFocused }
            .scale(scale),
        shape  = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = Primary,
            focusedContainerColor = Primary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(3.dp, Color.White.copy(alpha = 0.6f)),
                shape  = CircleShape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                // Иконки из UI-kit: ic_play (line) / ic_pause (fill)
                imageVector        = ImageVector.vectorResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                tint               = Color.White,
                modifier           = Modifier.size(30.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerIconButton(
    iconRes:           Int,
    contentDescription: String,
    size:              androidx.compose.ui.unit.Dp,
    onClick:           () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (isFocused) 1.08f else 1.0f, tween(150), label = "btn"
    )

    Surface(
        onClick  = onClick,
        modifier = Modifier
            .size(size)
            .onFocusChanged { isFocused = it.isFocused }
            .scale(scale),
        shape  = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor        = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White.copy(alpha = 0.22f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
                shape  = CircleShape
            )
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1.0f, focusedScale = 1.0f)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = ImageVector.vectorResource(iconRes),
                contentDescription = contentDescription,
                tint               = Color.White,
                modifier           = Modifier.size(26.dp)
            )
        }
    }
}

// ─── BottomBar ────────────────────────────────────────────────────────────────

@Composable
private fun BottomBar(
    isLive:        Boolean,
    playbackState: PlayerPlaybackState,
    modifier:      Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 60.dp, end = 60.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Прогрессбар
        if (!isLive && playbackState.durationMs > 0L) {
            ProgressBar(
                currentMs  = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                bufferedMs = playbackState.bufferedPositionMs
            )
        } else if (isLive) {
            LiveProgressBar()
        }

        // Время
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            if (isLive) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(LiveRed, CircleShape)
                    )
                    Text(
                        text  = "ПРЯМОЙ ЭФИР",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = LiveRed
                        )
                    )
                }
            } else {
                Text(
                    text  = formatTime(playbackState.currentPositionMs),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color    = Color.White
                    )
                )
                Text(
                    text  = formatTime(playbackState.durationMs),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 14.sp,
                        color    = Color.White.copy(alpha = 0.55f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(
    currentMs:  Long,
    durationMs: Long,
    bufferedMs: Long
) {
    val playedFraction   = if (durationMs > 0) (currentMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val bufferedFraction = if (durationMs > 0) (bufferedMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(2.dp))
    ) {
        // Буферизованная часть
        Box(
            modifier = Modifier
                .fillMaxWidth(bufferedFraction)
                .fillMaxHeight()
                .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
        )
        // Проигранная часть
        Box(
            modifier = Modifier
                .fillMaxWidth(playedFraction)
                .fillMaxHeight()
                .background(Primary, RoundedCornerShape(2.dp))
        )
        // Ползунок
        if (playedFraction > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = with(androidx.compose.ui.platform.LocalDensity.current) {
                        // Выравниваем ползунок по правому краю проигранного
                        0.dp // Упрощённо: будет recalculated через onGloballyPositioned
                    })
                    .fillMaxWidth(playedFraction)
                    .wrapContentWidth(Alignment.End)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun LiveProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.15f),
                        LiveRed.copy(alpha = 0.6f),
                        LiveRed
                    )
                ),
                RoundedCornerShape(2.dp)
            )
    )
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val h        = totalSec / 3600
    val m        = (totalSec % 3600) / 60
    val s        = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else              "%d:%02d".format(m, s)
}
