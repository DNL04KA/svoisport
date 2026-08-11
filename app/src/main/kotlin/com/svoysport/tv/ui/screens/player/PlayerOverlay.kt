package com.svoysport.tv.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.svoysport.tv.R
import com.svoysport.tv.ui.theme.LiveRed
import com.svoysport.tv.ui.theme.Primary

/** Минималистичный TV-оверлей по Figma Viewing / Player (1920×1080). */
@Composable
fun PlayerOverlay(
    isVisible: Boolean,
    title: String,
    isLive: Boolean,
    playbackState: PlayerPlaybackState,
    onPlayPause: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isVisible) {
        if (isVisible) runCatching { playFocusRequester.requestFocus() }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(220)),
        modifier = modifier.fillMaxSize()
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val uiScale = minOf(maxWidth.value / 1920f, maxHeight.value / 1080f, 1f)
                .coerceAtLeast(0.35f)

            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.42f),
                        0.22f to Color.Transparent,
                        0.72f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.72f)
                    )
                )
            ) {
                PlayerTopBar(
                    title = title,
                    onBack = onBack,
                    scale = uiScale,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                PrimaryPlaybackControl(
                    isPlaying = playbackState.isPlaying,
                    focusRequester = playFocusRequester,
                    onClick = onPlayPause,
                    scale = uiScale,
                    modifier = Modifier.align(Alignment.Center)
                )

                PlayerTimeline(
                    isLive = isLive,
                    playbackState = playbackState,
                    scale = uiScale,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerTopBar(
    title: String,
    onBack: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(start = (36f * scale).dp, top = (36f * scale).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((28f * scale).dp)
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(playerBackButtonSizeDp(scale).dp),
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Primary,
                focusedContainerColor = Primary
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.08f)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                    contentDescription = "Назад",
                    tint = Color.White,
                    modifier = Modifier.size((34f * scale).dp)
                )
            }
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = (32f * scale).coerceAtLeast(14f).sp,
            lineHeight = (40f * scale).coerceAtLeast(18f).sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(0.82f)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PrimaryPlaybackControl(
    isPlaying: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val focusScale by animateFloatAsState(
        if (focused) 1.06f else 1f,
        tween(140),
        label = "playerPrimaryControl"
    )
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(playerPrimaryControlWidthDp(scale).dp)
            .height((90f * scale).dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focused = it.isFocused }
            .scale(focusScale),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape((18f * scale).dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.30f),
            focusedContainerColor = Primary.copy(alpha = 0.94f)
        ),
        scale = ClickableSurfaceDefaults.scale(scale = 1f, focusedScale = 1f)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = if (isPlaying) "Пауза" else "Воспроизвести",
                tint = Color.White,
                modifier = Modifier.size((38f * scale).dp)
            )
        }
    }
}

@Composable
private fun PlayerTimeline(
    isLive: Boolean,
    playbackState: PlayerPlaybackState,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val horizontalPadding = (15f * scale).dp
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = horizontalPadding, end = horizontalPadding, bottom = (10f * scale).dp),
        verticalArrangement = Arrangement.spacedBy((10f * scale).dp)
    ) {
        if (isLive) {
            Box(
                Modifier.fillMaxWidth().height((6f * scale).dp)
                    .background(LiveRed, RoundedCornerShape((3f * scale).dp))
            )
        } else {
            PlaybackProgress(
                currentMs = playbackState.currentPositionMs,
                durationMs = playbackState.durationMs,
                bufferedMs = playbackState.bufferedPositionMs,
                scale = scale
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((18f * scale).dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    if (playbackState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((24f * scale).dp)
            )
            Box(Modifier.size((16f * scale).dp).background(Color.White))
            Text(
                text = if (isLive) "ПРЯМОЙ ЭФИР" else formatPlayerTimeline(
                    playbackState.currentPositionMs,
                    playbackState.durationMs
                ),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontSize = (16f * scale).coerceAtLeast(11f).sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun PlaybackProgress(
    currentMs: Long,
    durationMs: Long,
    bufferedMs: Long,
    scale: Float
) {
    val played = if (durationMs > 0L) (currentMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val buffered = if (durationMs > 0L) (bufferedMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Box(
        Modifier.fillMaxWidth().height((6f * scale).dp)
            .background(Color.White.copy(alpha = 0.28f), RoundedCornerShape((3f * scale).dp))
    ) {
        Box(
            Modifier.fillMaxWidth(buffered).fillMaxHeight()
                .background(Color.White.copy(alpha = 0.42f), RoundedCornerShape((3f * scale).dp))
        )
        Box(
            Modifier.fillMaxWidth(played).fillMaxHeight()
                .background(Color.White, RoundedCornerShape((3f * scale).dp))
        )
    }
}

internal fun playerBackButtonSizeDp(scale: Float): Float = 88f * scale

internal fun playerPrimaryControlWidthDp(scale: Float): Float = 144f * scale

internal fun formatPlayerTimeline(currentMs: Long, durationMs: Long): String =
    "${formatPlayerTime(currentMs)} / ${formatPlayerTime(durationMs)}"

private fun formatPlayerTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1_000L
    val hours = totalSeconds / 3_600L
    val minutes = (totalSeconds % 3_600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}
