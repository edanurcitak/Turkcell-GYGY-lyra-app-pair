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
 *
 * [isOwned] `true` ise (kullanıcının kendi listesi) düzenleme aksiyonları görünür: satırda şarkı
 * çıkarma + ekleme sayfası. Öne çıkan/Beğenilenler/İndirilenler listelerinde bunlar gizlidir.
 */
data class PlaylistDetailUiState(
    val title: String = "",
    val songs: List<Song> = emptyList(),
    val isOwned: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    // --- Şarkı ekleme sayfası (yalnızca owned listelerde) ---
    val showAddSheet: Boolean = false,
    val catalogSongs: List<Song> = emptyList(),
    val isCatalogLoading: Boolean = false,
    val selectedToAdd: Set<String> = emptySet(),
    val isAddingSongs: Boolean = false,
) {
    /** Ekleme sayfasındaki "Ekle (N)" düğmesinin sayacı. */
    val addSelectionCount: Int get() = selectedToAdd.size
}

sealed interface PlaylistDetailIntent {
    /** Şarkıları yeniden yükler (hata sonrası tekrar dene). */
    data object Retry : PlaylistDetailIntent

    /**
     * Kullanıcının aşağı çekme (pull-to-refresh) hareketi. [Retry] ile aynı yüklemeyi yapar ancak
     * tam-ekran spinner yerine üstteki dönen göstergeyi besler ([PlaylistDetailUiState.isRefreshing])
     * ve mevcut şarkı listesi görünür kalır.
     */
    data object PullRefresh : PlaylistDetailIntent

    /** Bir şarkıyı listeden çıkarır (yalnızca owned liste). */
    data class RemoveSong(val songId: String) : PlaylistDetailIntent

    /** Şarkı ekleme sayfasını açar ve katalağu (listede olmayan şarkılar) yükler. */
    data object OpenAddSheet : PlaylistDetailIntent

    /** Şarkı ekleme sayfasını kapatır (seçim sıfırlanır). */
    data object CloseAddSheet : PlaylistDetailIntent

    /** Ekleme sayfasında bir şarkının seçimini değiştirir (ekle/çıkar). */
    data class ToggleAddSelection(val songId: String) : PlaylistDetailIntent

    /** Seçili şarkıları listeye ekler ve sayfayı kapatır. */
    data object ConfirmAddSongs : PlaylistDetailIntent
}
