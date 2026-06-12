package com.turkcell.lyraapp.ui.feed

import com.turkcell.lyraapp.data.feed.MediaCard
import com.turkcell.lyraapp.data.feed.QuickPick

/**
 * Ana sayfa (feed) ekranının MVI sözleşmesi.
 *
 * - [FeedUiState]: ekranın tüm görünür durumu (single source of truth).
 * - [FeedIntent]: kullanıcı aksiyonları; ViewModel bunları işleyip yeni state üretir.
 */
data class FeedUiState(
    val greeting: String = "",
    val userInitials: String = "",
    val quickPicks: List<QuickPick> = emptyList(),
    val recentlyPlayed: List<MediaCard> = emptyList(),
    val playlists: List<MediaCard> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface FeedIntent {
    data object Refresh : FeedIntent
}
