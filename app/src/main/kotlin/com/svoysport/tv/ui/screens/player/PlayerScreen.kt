package com.svoysport.tv.ui.screens.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.svoysport.tv.ui.components.state.HomeErrorState
import com.svoysport.tv.ui.components.state.HomeLoadingState
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    onBack:    () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    // Флаг видимости оверлея
    var isOverlayVisible by remember { mutableStateOf(true) }

    // ── Авто-скрытие: через 5 сек после показа пока воспроизводится ──────────
    LaunchedEffect(isOverlayVisible, playbackState.isPlaying) {
        if (isOverlayVisible && playbackState.isPlaying) {
            delay(5_000)
            isOverlayVisible = false
        }
    }

    // ── BackHandler: 1-е нажатие Back → оверлей, 2-е → выход ─────────────────
    BackHandler {
        if (isOverlayVisible) {
            onBack()
        } else {
            isOverlayVisible = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // Любое нажатие D-pad → показываем оверлей; обрабатываем seek
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    isOverlayVisible = true
                    when (keyEvent.key) {
                        Key.DirectionCenter,
                        Key.Enter,
                        Key.NumPadEnter -> { viewModel.togglePlayPause(); true }
                        Key.DirectionLeft  -> { viewModel.seekRewind();     true }
                        Key.DirectionRight -> { viewModel.seekForward();    true }
                        else               -> false
                    }
                } else false
            }
            .focusable()
    ) {
        when (val state = uiState) {

            is PlayerUiState.Loading -> HomeLoadingState()

            is PlayerUiState.Error -> HomeErrorState(
                message = state.message,
                onRetry = { viewModel.loadPlayerData() }
            )

            is PlayerUiState.Ready -> {
                // ── AndroidView c PlayerView ──────────────────────────────────
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player        = viewModel.playerManager.player
                            useController = false  // кастомный оверлей
                            resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            layoutParams  = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    update = { pv ->
                        // Обновляем player-ссылку при изменении
                        if (pv.player !== viewModel.playerManager.player) {
                            pv.player = viewModel.playerManager.player
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ── Кастомный оверлей поверх видео ───────────────────────────
                PlayerOverlay(
                    isVisible     = isOverlayVisible,
                    title         = state.title,
                    competition   = state.competition,
                    isLive        = state.isLive,
                    playbackState = playbackState,
                    onPlayPause   = { viewModel.togglePlayPause() },
                    onSeekForward = { viewModel.seekForward() },
                    onSeekRewind  = { viewModel.seekRewind() },
                    onBack        = {
                        if (isOverlayVisible) onBack() else isOverlayVisible = true
                    }
                )
            }
        }
    }
}
