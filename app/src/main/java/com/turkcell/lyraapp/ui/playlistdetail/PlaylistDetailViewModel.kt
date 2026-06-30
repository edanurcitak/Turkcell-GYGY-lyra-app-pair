package com.turkcell.lyraapp.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Çalma listesi detay ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Seçilen listenin şarkılarını [PlaylistRepository] üzerinden yükler ve tek bir [StateFlow] ile
 * [PlaylistDetailUiState] yayınlar (FeedViewModel/PlayerViewModel ile aynı yükleme/hata deseni).
 *
 * playlistId, navigasyon argümanı olarak [SavedStateHandle]'dan okunur — `NavController`
 * ViewModel'e sızmaz (mevcut konvansiyon, bkz. PlayerViewModel).
 */
@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"]) {
        "PlaylistDetailViewModel için 'playlistId' nav argümanı zorunludur."
    }

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: PlaylistDetailIntent) {
        when (intent) {
            PlaylistDetailIntent.Retry -> load()
            PlaylistDetailIntent.PullRefresh -> load(isPull = true)
        }
    }

    /**
     * Şarkıları yükler. [isPull] `true` ise (pull-to-refresh) tam-ekran spinner gösterilmez; mevcut
     * liste görünür kalır, yenileme yalnızca üstteki dönen göstergeyle ([PlaylistDetailUiState.isRefreshing])
     * belirtilir ve başarısız tazelemede içerik korunur (FeedViewModel deseni).
     */
    private fun load(isPull: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (isPull) it.isLoading else true,
                    isRefreshing = isPull,
                    errorMessage = null,
                )
            }
            try {
                val detail = playlistRepository.getPlaylistDetail(playlistId)
                _uiState.update {
                    it.copy(
                        title = detail.name,
                        songs = detail.songs,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Pull-to-refresh'te liste varsa koru (sessiz başarısızlık); aksi halde hatayı göster.
                _uiState.update {
                    if (isPull && it.songs.isNotEmpty()) {
                        it.copy(isLoading = false, isRefreshing = false)
                    } else {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "Çalma listesi yüklenemedi. Lütfen tekrar deneyin.",
                        )
                    }
                }
            }
        }
    }
}
