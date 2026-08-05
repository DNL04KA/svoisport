package com.svoysport.tv.ui.screens.player

/** Состояние загрузки/готовности экрана плеера */
sealed class PlayerUiState {
    object Loading : PlayerUiState()
    data class Ready(
        val streamUrl:     String,
        val title:         String,
        val competition:   String,
        val isLive:        Boolean,
        val thumbnailUrl:  String
    ) : PlayerUiState()
    data class Waiting(
        val title: String,
        val competition: String,
        val startsAtMs: Long,
        val thumbnailUrl: String
    ) : PlayerUiState()
    data class Error(
        val message: String,
        val title: String = "",
        val thumbnailUrl: String = ""
    ) : PlayerUiState()
}

/** Состояние воспроизведения, обновляется каждые ~250 мс */
data class PlayerPlaybackState(
    val isPlaying:           Boolean = false,
    val currentPositionMs:   Long    = 0L,
    val durationMs:          Long    = 0L,   // 0 или отрицательное → лайв/неизвестно
    val bufferedPositionMs:  Long    = 0L,
    val exoState:            Int     = 1     // Player.STATE_IDLE = 1
)
