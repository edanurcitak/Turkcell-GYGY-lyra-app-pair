package com.turkcell.lyraapp.data.feed

/**
 * Ana sayfa (feed) domain modelleri.
 *
 * Kapak görselleri asset olmadığından [ArtworkTone] ile temsil edilir; UI katmanı bu tonu
 * `MaterialTheme.colorScheme` üzerinden bir renge çevirir (ham renk veri katmanında tutulmaz).
 */
enum class ArtworkTone { PRIMARY, SECONDARY, TERTIARY, NEUTRAL }

/** Bölüm 1: hızlı seçim tile'ı (yalnızca başlık + kapak tonu). */
data class QuickPick(
    val id: String,
    val title: String,
    val tone: ArtworkTone,
)

/** Bölüm 2 ve 3: başlık + alt başlık taşıyan medya kartı (son çalınan / çalma listesi). */
data class MediaCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val tone: ArtworkTone,
)

/** Ana sayfanın tüm içeriğini tek seferde taşıyan toplu model. */
data class HomeFeed(
    val greeting: String,
    val userInitials: String,
    val quickPicks: List<QuickPick>,
    val recentlyPlayed: List<MediaCard>,
    val playlists: List<MediaCard>,
)
