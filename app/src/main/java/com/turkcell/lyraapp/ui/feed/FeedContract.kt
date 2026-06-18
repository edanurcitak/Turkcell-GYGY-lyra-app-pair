package com.turkcell.lyraapp.ui.feed

import com.turkcell.lyraapp.data.feed.Song

/**
 * Ana sayfa (feed) ekranının MVI sözleşmesi (AGENTS.MD §4.2–§4.3).
 *
 * - [FeedUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [FeedIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 *
 * Not: [greeting] ve [userInitials] şarkı verisi değildir (API'da karşılığı yok); statik
 * varsayılan kalır. Yalnızca [songs] Streaming API'den yüklenir.
 */
data class FeedUiState(
    val greeting: String = "İyi akşamlar",
    val userInitials: String = "ZK",
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface FeedIntent {
    data object Refresh : FeedIntent
}
