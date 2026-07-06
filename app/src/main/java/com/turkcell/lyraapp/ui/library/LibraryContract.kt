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
    val errorMessage: String? = null,
    /** Silme onayı bekleyen (owned) liste; `null` ise dialog kapalı. */
    val pendingDelete: Playlist? = null,
    val isDeleting: Boolean = false,
)

sealed interface LibraryIntent {
    /** Liste ↔ ızgara görünümü arasında geçiş yapar. */
    data object ToggleViewMode : LibraryIntent

    /** Sıralama düzenini "Son eklenenler" ↔ "Alfabetik" arasında değiştirir. */
    data object ToggleSort : LibraryIntent

    /** Çalma listelerini yeniden yükler (hata sonrası tekrar dene). */
    data object Refresh : LibraryIntent

    /** Bir owned listeyi silmek için onay dialog'unu açar. */
    data class RequestDeletePlaylist(val playlist: Playlist) : LibraryIntent

    /** Onaylanan silmeyi gerçekleştirir. */
    data object ConfirmDeletePlaylist : LibraryIntent

    /** Silme onay dialog'unu kapatır (vazgeç). */
    data object DismissDeleteDialog : LibraryIntent

    /** Ekran tekrar öne geldiğinde (ör. oluşturma/detaydan dönüş) listeyi sessizce tazeler. */
    data object ScreenResumed : LibraryIntent
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
