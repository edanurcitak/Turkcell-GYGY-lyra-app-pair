package com.turkcell.lyraapp.ui.navigation

import android.net.Uri

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

    // Player ekranı songId path argümanı + title/artist query argümanları taşır:
    // "player/{songId}?title={title}&artist={artist}" (title/artist meta veriyi player'a iletir).
    const val PLAYER = "player"
    const val PLAYER_ARG_SONG_ID = "songId"
    const val PLAYER_ARG_TITLE = "title"
    const val PLAYER_ARG_ARTIST = "artist"
    const val PLAYER_ROUTE =
        "$PLAYER/{$PLAYER_ARG_SONG_ID}?$PLAYER_ARG_TITLE={$PLAYER_ARG_TITLE}&$PLAYER_ARG_ARTIST={$PLAYER_ARG_ARTIST}"

    /**
     * Belirli bir şarkı için somut player rotasını üretir.
     *
     * title/artist serbest metin olabildiğinden ([Uri.encode]) kodlanır; player ekranında
     * gösterilecek meta veriyi taşır (API'da tekil şarkı uç noktası olmadığından nav ile iletilir).
     */
    fun playerRoute(songId: String, title: String, artist: String): String =
        "$PLAYER/$songId?$PLAYER_ARG_TITLE=${Uri.encode(title)}&$PLAYER_ARG_ARTIST=${Uri.encode(artist)}"

    // Çalma listesi detayı playlistId argümanı taşır: "playlistDetail/{playlistId}".
    const val PLAYLIST_DETAIL = "playlistDetail"
    const val PLAYLIST_DETAIL_ARG_ID = "playlistId"
    const val PLAYLIST_DETAIL_ROUTE = "$PLAYLIST_DETAIL/{$PLAYLIST_DETAIL_ARG_ID}"

    /** Belirli bir çalma listesi için somut detay rotasını üretir. */
    fun playlistDetailRoute(playlistId: String): String = "$PLAYLIST_DETAIL/$playlistId"

    // Yeni çalma listesi oluşturma (tam ekran; argümansız).
    const val CREATE_PLAYLIST = "createPlaylist"
}
