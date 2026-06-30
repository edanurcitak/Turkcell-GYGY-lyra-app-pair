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

    // OTP (doğrulama kodu) ekranı, doğrulanacak numarayı path argümanı olarak taşır:
    // "otp/{phone}". phone, E.164-ish ("+90…") biçimindedir ve "+" içerdiğinden kodlanır.
    const val OTP = "otp"
    const val OTP_ARG_PHONE = "phone"
    const val OTP_ROUTE = "$OTP/{$OTP_ARG_PHONE}"

    /** Belirli bir numara için somut OTP rotasını üretir ("+" içerdiğinden [Uri.encode]). */
    fun otpRoute(phone: String): String = "$OTP/${Uri.encode(phone)}"

    // Player ekranı songId path argümanı + title/artist/queue query argümanları taşır:
    // "player/{songId}?title={title}&artist={artist}&queue={queue}".
    // title/artist meta veriyi; queue ise önceki/sonraki için kuyruk kaynağını ("feed" veya
    // "playlist:<id>") player'a iletir.
    const val PLAYER = "player"
    const val PLAYER_ARG_SONG_ID = "songId"
    const val PLAYER_ARG_TITLE = "title"
    const val PLAYER_ARG_ARTIST = "artist"
    const val PLAYER_ARG_QUEUE = "queue"
    const val PLAYER_ROUTE =
        "$PLAYER/{$PLAYER_ARG_SONG_ID}?$PLAYER_ARG_TITLE={$PLAYER_ARG_TITLE}" +
            "&$PLAYER_ARG_ARTIST={$PLAYER_ARG_ARTIST}&$PLAYER_ARG_QUEUE={$PLAYER_ARG_QUEUE}"

    /** Kuyruk kaynağı: ana sayfadaki şarkı kataloğu. */
    const val QUEUE_FEED = "feed"

    /** Kuyruk kaynağı öneki: belirli bir çalma listesi ("playlist:<id>"). */
    const val QUEUE_PLAYLIST_PREFIX = "playlist:"

    /** Çalma listesi için kuyruk kaynağı değeri üretir. */
    fun queuePlaylist(playlistId: String): String = "$QUEUE_PLAYLIST_PREFIX$playlistId"

    /**
     * Belirli bir şarkı için somut player rotasını üretir.
     *
     * title/artist serbest metin olabildiğinden ([Uri.encode]) kodlanır; player ekranında
     * gösterilecek meta veriyi taşır (API'da tekil şarkı uç noktası olmadığından nav ile iletilir).
     * [queue], önceki/sonraki butonlarının gezeceği listeyi belirler ([QUEUE_FEED] veya
     * [queuePlaylist]).
     */
    fun playerRoute(
        songId: String,
        title: String,
        artist: String,
        queue: String = QUEUE_FEED,
    ): String =
        "$PLAYER/$songId?$PLAYER_ARG_TITLE=${Uri.encode(title)}" +
            "&$PLAYER_ARG_ARTIST=${Uri.encode(artist)}&$PLAYER_ARG_QUEUE=${Uri.encode(queue)}"

    // Çalma listesi detayı playlistId argümanı taşır: "playlistDetail/{playlistId}".
    const val PLAYLIST_DETAIL = "playlistDetail"
    const val PLAYLIST_DETAIL_ARG_ID = "playlistId"
    const val PLAYLIST_DETAIL_ROUTE = "$PLAYLIST_DETAIL/{$PLAYLIST_DETAIL_ARG_ID}"

    /** Belirli bir çalma listesi için somut detay rotasını üretir. */
    fun playlistDetailRoute(playlistId: String): String = "$PLAYLIST_DETAIL/$playlistId"

    // Yeni çalma listesi oluşturma (tam ekran; argümansız).
    const val CREATE_PLAYLIST = "createPlaylist"

    // Premium plan seçim ekranı (tam ekran; argümansız). Profil'deki free banner'dan açılır.
    const val PREMIUM_PLANS = "premiumPlans"

    // Ödeme ekranı (tam ekran); satın alınacak planın id'sini path argümanı olarak taşır:
    // "payment/{planId}". Premium plan ekranındaki "Devam et"ten açılır.
    const val PAYMENT = "payment"
    const val PAYMENT_ARG_PLAN_ID = "planId"
    const val PAYMENT_ROUTE = "$PAYMENT/{$PAYMENT_ARG_PLAN_ID}"

    /** Belirli bir plan için somut ödeme rotasını üretir (planId katalog id'sidir, kodlama gerekmez). */
    fun paymentRoute(planId: String): String = "$PAYMENT/$planId"

    // Ödeme başarılı ekranı (tam ekran; argümansız). Ödeme onayından sonra açılır.
    const val PAYMENT_SUCCESS = "paymentSuccess"
}
