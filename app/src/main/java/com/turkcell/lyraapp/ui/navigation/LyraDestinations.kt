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
}
