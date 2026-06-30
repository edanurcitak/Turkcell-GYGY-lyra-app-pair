package com.turkcell.lyraapp.ui.library

import com.turkcell.lyraapp.data.playlist.Playlist

/**
 * Kütüphane ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [LibraryUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [LibraryIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Çalma listeleri Streaming API'den ([com.turkcell.lyraapp.data.playlist.PlaylistRepository])
 * yüklenir (FeedUiState ile aynı yükleme/hata deseni). Filtre çipleri salt görseldir (seçili
 * sekme sabittir); görünüm modu ([viewMode]) ve sıralama ([sortOrder]) state'e bağlıdır.
 * Navigasyon/Effect talep edilmediğinden eklenmez (§4.6).
 */
data class LibraryUiState(
    val playlists: List<Playlist> = emptyList(),
    val viewMode: LibraryViewMode = LibraryViewMode.LIST,
    val sortOrder: LibrarySortOrder = LibrarySortOrder.RECENT,
    val isOffline: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LibraryIntent {
    /** Liste ↔ ızgara görünümü arasında geçiş yapar. */
    data object ToggleViewMode : LibraryIntent

    /** Sıralama düzenini "Son eklenenler" ↔ "Alfabetik" arasında değiştirir. */
    data object ToggleSort : LibraryIntent

    /** Çalma listelerini yeniden yükler (hata sonrası tekrar dene). */
    data object Refresh : LibraryIntent

    /**
     * Kullanıcının aşağı çekme (pull-to-refresh) hareketi. [Refresh] ile aynı yüklemeyi yapar ancak
     * tam-ekran spinner yerine üstteki dönen göstergeyi besler ([LibraryUiState.isRefreshing]) ve
     * mevcut liste görünür kalır.
     */
    data object PullRefresh : LibraryIntent
}

/** Üstteki filtre sekmeleri (salt görsel; seçili sekme sabittir). */
enum class LibraryFilter(val label: String) {
    PLAYLISTS("Çalma listeleri"),
    ARTISTS("Sanatçılar"),
    ALBUMS("Albümler"),
}

/** İçerik yerleşimi: dikey liste veya 2 sütunlu ızgara. */
enum class LibraryViewMode { LIST, GRID }

/** Sıralama düzeni; etiketi sıralama satırında gösterilir. */
enum class LibrarySortOrder(val label: String) {
    RECENT("Son eklenenler"),
    ALPHABETICAL("Alfabetik (A-Z)"),
}
