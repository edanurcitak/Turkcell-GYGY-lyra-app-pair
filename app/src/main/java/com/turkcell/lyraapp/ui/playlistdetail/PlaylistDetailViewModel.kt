package com.turkcell.lyraapp.ui.playlistdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.util.ErrorContext
import com.turkcell.lyraapp.util.toAppError
import com.turkcell.lyraapp.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
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
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val playlistId: String = checkNotNull(savedStateHandle["playlistId"]) {
        "PlaylistDetailViewModel için 'playlistId' nav argümanı zorunludur."
    }

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        observeFavoritesIfLiked()
    }

    /**
     * "Beğenilen Şarkılar" listesi açıkken favori kümesi ([FavoritesRepository.likedIds]) değişirse
     * şarkı listesini sessizce tazeler — böylece ekran dinamik kalır (elle yenileme gerekmez). İlk
     * emisyon atlanır ([drop]); ilk içeriği zaten [load] getirir. Diğer listeler için etkisizdir.
     */
    private fun observeFavoritesIfLiked() {
        if (playlistId != PlaylistRepository.LIKED_PLAYLIST_ID) return
        viewModelScope.launch {
            favoritesRepository.likedIds
                .drop(1)
                .collect { reloadSilently() }
        }
    }

    /** Şarkı listesini tam-ekran spinner göstermeden günceller (reaktif favori değişimi için). */
    private fun reloadSilently() {
        viewModelScope.launch {
            runCatching { playlistRepository.getPlaylistDetail(playlistId) }
                .onSuccess { detail ->
                    _uiState.update { it.copy(title = detail.name, songs = detail.songs) }
                }
        }
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
                            errorMessage = e.toAppError().toUserMessage(ErrorContext.PLAYLIST_DETAIL),
                        )
                    }
                }
            }
        }
    }
}
