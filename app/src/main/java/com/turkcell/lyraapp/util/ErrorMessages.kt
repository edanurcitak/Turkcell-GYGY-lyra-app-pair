package com.turkcell.lyraapp.util

/**
 * Hatanın hangi ekran/akış bağlamında oluştuğunu belirtir.
 *
 * Aynı HTTP kodu (ör. 400, 401, 403, 404, 409) uca göre farklı anlam taşıdığından, [toUserMessage]
 * doğru kullanıcı metnini seçmek için bu bağlamı kullanır. Değerler `docs/api/openapi.json`'daki
 * uçlarla eşleşen `ui/` ekran/akışlarıdır.
 */
enum class ErrorContext {
    GENERIC, LOGIN, OTP, REGISTER, HOME, SEARCH,
    LIBRARY, PLAYLIST_DETAIL, CREATE_PLAYLIST, PAYMENT, PREMIUM, PLAYER
}

/**
 * [AppError]'ı kullanıcıya gösterilebilir Türkçe bir mesaja çevirir.
 *
 * Ağ ve bilinmeyen hatalar bağlamdan bağımsızdır; API hataları ise [ErrorContext] + HTTP koduna
 * göre [resolveApiMessage] içinde çözülür.
 */
fun AppError.toUserMessage(context: ErrorContext = ErrorContext.GENERIC): String = when (this) {
    is AppError.Network -> "İnternet bağlantısı yok"
    is AppError.Unknown -> message ?: "Bilinmeyen bir hata oluştu."
    is AppError.Api -> resolveApiMessage(code, context)
}

/**
 * HTTP durum kodu + bağlamdan kullanıcı metni üretir. Kaynak: `docs/api/openapi.json` uç yanıtları.
 *
 * Sıra önemlidir (ilk eşleşen kazanır): önce sunucu (5xx), sonra bağlama özgü net eşlemeler,
 * en sonda ise her uçta ortak olan koda göre genel yedekler.
 */
private fun resolveApiMessage(code: Int, context: ErrorContext): String = when {
    // Tüm uçlar için ortak: sunucu tarafı hata.
    code in 500..599 -> "Sunucu şu anda cevap veremiyor, lütfen sonra tekrar dene"

    // auth/otp/request → 400 (Invalid phone number)
    context == ErrorContext.LOGIN && code == 400 -> "Geçersiz telefon numarası"

    // auth/otp/verify → 400 (Missing/invalid phone or code), 401 (Invalid OTP code)
    context == ErrorContext.OTP && code == 400 -> "Telefon numarası veya kod geçersiz"
    context == ErrorContext.OTP && code == 401 -> "Girdiğin kod hatalı veya süresi dolmuş"

    // me/update-informations → 400 (Invalid name or birth date)
    context == ErrorContext.REGISTER && code == 400 -> "Geçersiz ad, soyad veya doğum tarihi"

    // songs?q= → 400 (Invalid limit, cursor or search query)
    context == ErrorContext.SEARCH && code == 400 -> "Geçersiz arama isteği"

    // POST me/playlists → 400 (Invalid name or description)
    context == ErrorContext.CREATE_PLAYLIST && code == 400 -> "Geçersiz liste adı veya açıklaması"

    // DELETE me/playlists/{id} → 403 (not owner), 404 (not found)
    context == ErrorContext.LIBRARY && code == 403 -> "Bu çalma listesi sana ait değil"
    context == ErrorContext.LIBRARY && code == 404 -> "Çalma listesi bulunamadı"

    // playlists/{id} + tracks uçları → 403 (not owner), 404 (playlist/song not found), 409 (already in)
    context == ErrorContext.PLAYLIST_DETAIL && code == 403 -> "Bu çalma listesi sana ait değil"
    context == ErrorContext.PLAYLIST_DETAIL && code == 404 -> "Çalma listesi veya şarkı bulunamadı"
    context == ErrorContext.PLAYLIST_DETAIL && code == 409 -> "Şarkı zaten bu çalma listesinde"

    // memberships/checkout → 400 (invalid plan/card), 402 (payment declined)
    context == ErrorContext.PAYMENT && code == 400 -> "Geçersiz plan veya kart bilgisi"
    context == ErrorContext.PAYMENT && code == 402 -> "Ödeme reddedildi, kart bilgilerini kontrol et"

    // songs/{id}/stream-url → 403 (free tier), 404 (song not found); stream/{id} → 410 (expired)
    context == ErrorContext.PLAYER && code == 403 -> "Bu içerik için premium üyelik gerekli"
    context == ErrorContext.PLAYER && code == 404 -> "Şarkı bulunamadı"
    context == ErrorContext.PLAYER && code == 410 -> "Yayın bağlantısının süresi doldu, tekrar dene"

    // Koda göre genel yedekler (her korumalı uçta ortak; bağlama özgü metin yoksa buraya düşer).
    code == 400 -> "Geçersiz istek"
    code == 401 -> "Oturumun sona erdi, lütfen tekrar giriş yap"
    code == 402 -> "Ödeme reddedildi"
    code == 403 -> "Bu işlem için yetkin yok"
    code == 404 -> "İstenen kayıt bulunamadı"
    code == 409 -> "İşlem çakıştı, lütfen tekrar dene"
    code == 410 -> "Bağlantının süresi doldu, lütfen tekrar dene"

    else -> "Beklenmeyen bir hata oluştu"
}
