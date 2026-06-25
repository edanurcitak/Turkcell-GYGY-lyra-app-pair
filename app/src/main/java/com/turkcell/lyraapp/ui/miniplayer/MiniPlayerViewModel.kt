package com.turkcell.lyraapp.ui.miniplayer

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.turkcell.lyraapp.ui.player.PlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Docked mini player'ın MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Tam ekran [com.turkcell.lyraapp.ui.player.PlayerViewModel] gibi, oynatmaya sahip olmaz; yalnızca
 * [PlaybackService]'e bir [MediaController] ile **bağlanıp gözlemler**. Aktif parça/çalma durumu
 * (bildirimden veya tam ekran player'dan değişse bile) [Player.Listener.onEvents] ile tek bir
 * [StateFlow] üzerinden [MiniPlayerUiState]'e yansıtılır. Kuyruk boşken çubuk gizlenir
 * ([MiniPlayerUiState.isVisible] = false).
 *
 * Kontroller (önceki / oynat-duraklat / sonraki) doğrudan controller'a iletilir.
 */
@HiltViewModel
class MiniPlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiniPlayerUiState())
    val uiState: StateFlow<MiniPlayerUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val playerListener = object : Player.Listener {
        // Tekil olaylar yerine toplu olayları dinleyip durumu controller'dan okuruz (sade + sağlam).
        override fun onEvents(player: Player, events: Player.Events) {
            syncFrom(player)
        }
    }

    init {
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                val c = future.get()
                controller = c
                c.addListener(playerListener)
                syncFrom(c)
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun onIntent(intent: MiniPlayerIntent) {
        when (intent) {
            MiniPlayerIntent.PlayPause -> controller?.let { if (it.isPlaying) it.pause() else it.play() }
            MiniPlayerIntent.SkipPrevious -> controller?.seekToPrevious()
            MiniPlayerIntent.SkipNext -> controller?.seekToNext()
        }
    }

    /** Controller'ın güncel durumunu (aktif parça, çalıyor mu, kuyruk dolu mu) state'e yansıtır. */
    private fun syncFrom(player: Player) {
        val current = player.currentMediaItem
        if (current == null || player.mediaItemCount == 0) {
            _uiState.update { it.copy(isVisible = false, isPlaying = false) }
            return
        }
        _uiState.update {
            it.copy(
                isVisible = true,
                songId = current.mediaId.ifEmpty { it.songId },
                title = current.mediaMetadata.title?.toString() ?: it.title,
                artist = current.mediaMetadata.artist?.toString() ?: it.artist,
                isPlaying = player.isPlaying,
            )
        }
    }

    override fun onCleared() {
        // Controller'ı bırak; ExoPlayer'ı DEĞİL — servis arka planda çalmaya devam etsin.
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }
}
