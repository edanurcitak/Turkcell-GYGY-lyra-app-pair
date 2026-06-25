package com.turkcell.lyraapp.ui.miniplayer

/**
 * Docked mini player'ın MVI sözleşmesi (AGENTS.MD §4.2 / §4.3).
 *
 * Mini player, tam ekran [com.turkcell.lyraapp.ui.player.PlayerScreen]'den bağımsız olarak ana
 * kabukta (BNB üstünde) kalıcıdır ve aynı [com.turkcell.lyraapp.ui.player.PlaybackService]'e bir
 * [androidx.media3.session.MediaController] ile bağlanarak "şu an çalan"ı yansıtır.
 *
 * [isVisible] yalnızca kuyrukta aktif bir parça varken `true`'dur; aksi halde çubuk gizlenir.
 */
data class MiniPlayerUiState(
    val songId: String = "",
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val isVisible: Boolean = false,
)

sealed interface MiniPlayerIntent {
    /** Oynat/duraklat (işlevsel — MediaController üzerinden ExoPlayer'a bağlı). */
    data object PlayPause : MiniPlayerIntent

    /** Kuyrukta önceki şarkı (işlevsel — kuyruk bağlamına bağlı). */
    data object SkipPrevious : MiniPlayerIntent

    /** Kuyrukta sonraki şarkı (işlevsel — kuyruk bağlamına bağlı). */
    data object SkipNext : MiniPlayerIntent
}
