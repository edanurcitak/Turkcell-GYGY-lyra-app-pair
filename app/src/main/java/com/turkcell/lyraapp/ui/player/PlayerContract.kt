package com.turkcell.lyraapp.ui.player

/**
 * Player ekranının MVI sözleşmesi (AGENTS.MD §4.2 / §4.3).
 *
 * Özel "Şimdi Çalıyor" arayüzü ExoPlayer'ı doğrudan sürer; bu yüzden [PlayerUiState]
 * oynatma durumunu (çalıyor mu, pozisyon, süre) ve gösterilecek meta veriyi tutar.
 *
 * Not (§2.2): [songId] yalnızca kapak rengini deterministik türetmek içindir (Feed/PlaylistDetail
 * ile aynı `artworkColorFor` yaklaşımı). [title]/[artist] navigasyon argümanı olarak gelir;
 * çalma listesi/kuyruk bağlamı player'a taşınmadığından üst barda yalnızca "Şimdi Çalıyor" yazar.
 */
data class PlayerUiState(
    val songId: String = "",
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface PlayerIntent {
    /** Oynat/duraklat (işlevsel — ExoPlayer'a bağlı). */
    data object PlayPause : PlayerIntent

    /** İlerleme çubuğundan konuma atla (işlevsel — ExoPlayer'a bağlı). */
    data class SeekTo(val positionMs: Long) : PlayerIntent

    /** Kuyrukta sonraki şarkı (işlevsel — kuyruk bağlamına bağlı). */
    data object SkipNext : PlayerIntent

    /** Kuyrukta önceki şarkı (işlevsel — kuyruk bağlamına bağlı). */
    data object SkipPrevious : PlayerIntent

    /** Aktif şarkıyı cihaza indir (çevrimdışı çalma için cache'e yazar). */
    data object Download : PlayerIntent

    /** Hata sonrası yeniden yükle. */
    data object Retry : PlayerIntent
}
