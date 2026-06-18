package com.turkcell.lyraapp.ui.navigation

/**
 * Uygulamanın navigasyon rota (route) sabitleri.
 *
 * İskelet yapı: her ekran tek bir string route ile temsil edilir. Yeni ekranlar
 * eklendikçe buraya genişletilir.
 */
object LyraDestinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    // Player ekranı songId argümanı taşır: "player/{songId}".
    const val PLAYER = "player"
    const val PLAYER_ARG_SONG_ID = "songId"
    const val PLAYER_ROUTE = "$PLAYER/{$PLAYER_ARG_SONG_ID}"

    /** Belirli bir şarkı için somut player rotasını üretir. */
    fun playerRoute(songId: String): String = "$PLAYER/$songId"

    // Çalma listesi detayı playlistId argümanı taşır: "playlistDetail/{playlistId}".
    const val PLAYLIST_DETAIL = "playlistDetail"
    const val PLAYLIST_DETAIL_ARG_ID = "playlistId"
    const val PLAYLIST_DETAIL_ROUTE = "$PLAYLIST_DETAIL/{$PLAYLIST_DETAIL_ARG_ID}"

    /** Belirli bir çalma listesi için somut detay rotasını üretir. */
    fun playlistDetailRoute(playlistId: String): String = "$PLAYLIST_DETAIL/$playlistId"
}
