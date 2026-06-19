package com.turkcell.lyraapp.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.ui.navigation.LyraDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Player ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * ExoPlayer örneğinin sahibidir (rotasyonda oynatma kesilmesin diye). `stream-url` ucundan
 * imzalı URL'i çeker, [MediaItem] olarak hazırlar ve oynatmaya başlar. Oynatma durumu
 * (çalıyor mu, pozisyon, süre) [uiState]'e yansıtılır; özel Compose arayüzü bu duruma bağlanır.
 *
 * songId/title/artist navigasyon argümanı olarak [SavedStateHandle]'dan okunur — `NavController`
 * ViewModel'e sızmaz (mevcut konvansiyon). title/artist henüz route'a eklenmemişse boş kalır.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[LyraDestinations.PLAYER_ARG_SONG_ID]) {
        "PlayerViewModel için 'songId' nav argümanı zorunludur."
    }
    private val title: String = savedStateHandle[LyraDestinations.PLAYER_ARG_TITLE] ?: ""
    private val artist: String = savedStateHandle[LyraDestinations.PLAYER_ARG_ARTIST] ?: ""

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
    }

    private val _uiState = MutableStateFlow(
        PlayerUiState(songId = songId, title = title, artist = artist),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        observePlayback()
        load()
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.PlayPause -> if (player.isPlaying) player.pause() else player.play()
            is PlayerIntent.SeekTo -> player.seekTo(intent.positionMs)
            PlayerIntent.Retry -> load()
        }
    }

    /** ExoPlayer durumunu (çalıyor mu) dinler ve pozisyon/süreyi periyodik yansıtır. */
    private fun observePlayback() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
        })
        viewModelScope.launch {
            while (isActive) {
                val duration = player.duration.let { if (it > 0) it else 0L }
                _uiState.update {
                    it.copy(
                        positionMs = player.currentPosition.coerceAtLeast(0L),
                        durationMs = duration,
                    )
                }
                delay(POSITION_POLL_INTERVAL_MS)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val stream = songRepository.getStreamUrl(songId)
                player.setMediaItem(MediaItem.fromUri(stream.url))
                player.prepare()
                player.playWhenReady = true
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Şarkı yüklenemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }

    override fun onCleared() {
        player.release()
    }

    private companion object {
        const val POSITION_POLL_INTERVAL_MS = 500L
    }
}
