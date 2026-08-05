package com.svoysport.tv.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svoysport.tv.domain.repository.MatchRepository
import com.svoysport.tv.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEEK_STEP_MS = 10_000L   // ±10 секунд на перемотку

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: MatchRepository,
    val playerManager: PlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val matchId: String = checkNotNull(savedStateHandle["matchId"])

    // ── UI state ──────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Loading)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // ── Playback state (позиция обновляется каждые 250 мс) ───────────────────

    private val _playbackState = MutableStateFlow(PlayerPlaybackState())
    val playbackState: StateFlow<PlayerPlaybackState> = _playbackState.asStateFlow()

    init {
        loadPlayerData()
        startPlaybackPoller()
    }

    // ─────────────────────────────────────────────────────────────────────────

    fun loadPlayerData() {
        viewModelScope.launch {
            _uiState.value = PlayerUiState.Loading
            repository.getMatchDetails(matchId)
                .onSuccess { match ->
                    if (!match.isLive && match.durationSec == 0L && match.startTimeMs <= System.currentTimeMillis()) {
                        _uiState.value = PlayerUiState.Error("Трансляция завершена")
                        return@onSuccess
                    }
                    val url = match.streamUrl
                    if (url.isNullOrBlank()) {
                        val startsInMs = match.startTimeMs - System.currentTimeMillis()
                        if (!match.isLive && startsInMs in 1..300_000L) {
                            _uiState.value = PlayerUiState.Waiting(
                                title = match.title,
                                competition = match.competition.name,
                                startsAtMs = match.startTimeMs,
                                thumbnailUrl = match.backgroundUrl ?: match.thumbnailUrl
                            )
                            viewModelScope.launch {
                                delay(startsInMs + 500L)
                                loadPlayerData()
                            }
                            return@onSuccess
                        }
                        _uiState.value = PlayerUiState.Error("Трансляция недоступна")
                        return@onSuccess
                    }
                    _uiState.value = PlayerUiState.Ready(
                        streamUrl   = url,
                        title       = match.title,
                        competition = match.competition.name,
                        isLive      = match.isLive,
                        thumbnailUrl = match.thumbnailUrl
                    )
                    playerManager.initializePlayer()
                    playerManager.prepare(url)
                }
                .onFailure { e ->
                    _uiState.value = PlayerUiState.Error(e.message ?: "Ошибка загрузки")
                }
        }
    }

    /** Каждые 250 мс читает позицию из ExoPlayer и обновляет StateFlow */
    private fun startPlaybackPoller() {
        viewModelScope.launch {
            while (isActive) {
                delay(250)
                playerManager.player?.let { p ->
                    _playbackState.value = PlayerPlaybackState(
                        isPlaying          = p.isPlaying,
                        currentPositionMs  = p.currentPosition,
                        durationMs         = p.duration.coerceAtLeast(0L),
                        bufferedPositionMs = p.bufferedPosition,
                        exoState           = p.playbackState
                    )
                }
            }
        }
    }

    // ── Playback controls ─────────────────────────────────────────────────────

    fun togglePlayPause() {
        val p = playerManager.player ?: return
        if (p.isPlaying) playerManager.pause() else playerManager.play()
    }

    fun seekForward()  = playerManager.player?.let { p ->
        playerManager.seekTo((p.currentPosition + SEEK_STEP_MS).coerceAtMost(p.duration.coerceAtLeast(0)))
    }

    fun seekRewind()   = playerManager.player?.let { p ->
        playerManager.seekTo((p.currentPosition - SEEK_STEP_MS).coerceAtLeast(0))
    }

    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)

    // ─────────────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
