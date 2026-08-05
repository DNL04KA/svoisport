package com.svoysport.tv.ui.screens.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.svoysport.tv.R
import com.svoysport.tv.ui.components.state.HomeLoadingState
import com.svoysport.tv.ui.theme.Gray3
import com.svoysport.tv.ui.theme.Primary
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

            is PlayerUiState.Error -> PlayerErrorState(
                message = state.message,
                onRetry = { viewModel.loadPlayerData() },
                onBack = onBack
            )

            is PlayerUiState.Waiting -> PlayerWaitingState(state = state, onBack = onBack)

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

@Composable
private fun PlayerWaitingState(state: PlayerUiState.Waiting, onBack: () -> Unit) {
    var remainingMs by remember(state.startsAtMs) {
        mutableLongStateOf((state.startsAtMs - System.currentTimeMillis()).coerceAtLeast(0L))
    }
    LaunchedEffect(state.startsAtMs) {
        while (remainingMs > 0L) {
            delay(1_000)
            remainingMs = (state.startsAtMs - System.currentTimeMillis()).coerceAtLeast(0L)
        }
    }
    val minutes = remainingMs / 60_000L
    val seconds = (remainingMs / 1_000L) % 60L

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = state.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.42f
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.44f)))
        PlayerExitButton(onBack = onBack, modifier = Modifier.align(Alignment.TopStart).padding(40.dp))
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Начало через", color = Gray3, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "%02d:%02d".format(minutes, seconds),
                color = Color.White,
                fontSize = 64.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(28.dp))
            Text(state.title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(
                "Трансляция начнётся автоматически. Вам не нужно обновлять экран",
                color = Gray3,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun PlayerErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    val retryFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { retryFocusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        PlayerExitButton(onBack = onBack, modifier = Modifier.align(Alignment.TopStart).padding(40.dp))

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (message.contains("регион", ignoreCase = true)) {
                    "Трансляция недоступна в вашем регионе"
                } else {
                    "Трансляция не открылась"
                },
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(text = message, color = Gray3, fontSize = 18.sp)
            if (message.contains("регион", ignoreCase = true)) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Некоторые трансляции ограничены правами показа. Поддержка: info@sport-tv.by",
                    color = Gray3,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.focusRequester(retryFocusRequester),
                shape = ButtonDefaults.shape(RoundedCornerShape(18.dp)),
                colors = ButtonDefaults.colors(
                    containerColor = Color(0xFF343746),
                    focusedContainerColor = Primary
                ),
                scale = ButtonDefaults.scale(focusedScale = 1.06f)
            ) {
                Text("Повторить попытку", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PlayerExitButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onBack,
        modifier = modifier,
        shape = ButtonDefaults.shape(RoundedCornerShape(18.dp)),
        colors = ButtonDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Primary
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Text("Выйти", fontSize = 18.sp)
        }
    }
}
