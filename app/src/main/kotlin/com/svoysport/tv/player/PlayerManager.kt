package com.svoysport.tv.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.svoysport.tv.session.SettingsManager

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var _player: ExoPlayer? = null
    val player: ExoPlayer? get() = _player

    @OptIn(UnstableApi::class)
    fun initializePlayer(): ExoPlayer {
        if (_player == null) {
            // sport-tv.by отдаёт HLS только с корректными заголовками
            // (User-Agent + Referer), иначе 403 / пустой ответ.
            val httpFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("SvoySportTV/1.0 (Android TV)")
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(mapOf("Referer" to "https://sport-tv.by/"))

            _player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(httpFactory))
                .build()
                .apply {
                    playWhenReady = true
                    SettingsManager.maxVideoHeight()?.let { height ->
                        trackSelectionParameters = trackSelectionParameters.buildUpon()
                            .setMaxVideoSize(Int.MAX_VALUE, height)
                            .build()
                    }
                }
        }
        return _player!!
    }

    fun prepare(url: String) {
        _player?.let { p ->
            val mediaItem = MediaItem.fromUri(url)
            p.setMediaItem(mediaItem)
            p.prepare()
        }
    }

    fun release() {
        _player?.release()
        _player = null
    }

    fun play() = _player?.play()
    fun pause() = _player?.pause()
    fun seekTo(positionMs: Long) = _player?.seekTo(positionMs)
}
