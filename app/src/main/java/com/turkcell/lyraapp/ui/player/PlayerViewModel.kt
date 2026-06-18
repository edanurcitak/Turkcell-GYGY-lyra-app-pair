package com.turkcell.lyraapp.ui.player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.feed.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Player ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * ExoPlayer örneğinin sahibidir (rotasyonda oynatma kesilmesin diye). `stream-url` ucundan
 * imzalı URL'i çeker, [MediaItem] olarak hazırlar ve oynatmaya başlar. [player] doğrudan
 * `PlayerView`'e bağlanır; örnek [onCleared]'da serbest bırakılır.
 *
 * songId, navigasyon argümanı olarak [SavedStateHandle]'dan okunur — `NavController` ViewModel'e
 * sızmaz (mevcut konvansiyon).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle["songId"]) {
        "PlayerViewModel için 'songId' nav argümanı zorunludur."
    }

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build(),
            /* handleAudioFocus = */ true,
        )
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.Retry -> load()
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
}
