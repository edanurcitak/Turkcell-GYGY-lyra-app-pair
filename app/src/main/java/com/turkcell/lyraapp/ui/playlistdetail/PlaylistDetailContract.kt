package com.turkcell.lyraapp.ui.playlistdetail

import com.turkcell.lyraapp.data.feed.Song

/**
 * Çalma listesi detay ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [PlaylistDetailUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [PlaylistDetailIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Şarkılar Streaming API'den ([com.turkcell.lyraapp.data.playlist.PlaylistRepository]) yüklenir
 * (FeedUiState/PlayerUiState ile aynı yükleme/hata deseni).
 */
data class PlaylistDetailUiState(
    val title: String = "",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PlaylistDetailIntent {
    /** Şarkıları yeniden yükler (hata sonrası tekrar dene). */
    data object Retry : PlaylistDetailIntent

    /**
     * Kullanıcının aşağı çekme (pull-to-refresh) hareketi. [Retry] ile aynı yüklemeyi yapar ancak
     * tam-ekran spinner yerine üstteki dönen göstergeyi besler ([PlaylistDetailUiState.isRefreshing])
     * ve mevcut şarkı listesi görünür kalır.
     */
    data object PullRefresh : PlaylistDetailIntent
}
