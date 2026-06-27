package com.turkcell.lyraapp.data.auth

/**
 * Auth (kimlik) domain modelleri.
 *
 * Streaming API'nin `User` / `AuthTokens` / `AuthSession` şemalarının (bkz.
 * `docs/api/openapi.json`) uygulama-içi karşılıklarıdır. Veri kaynağından bağımsızdır;
 * yalnızca uygulamanın ihtiyaç duyduğu alanları tutar.
 */

/**
 * Oturum açan kullanıcı.
 *
 * [profileCompleted]: firstName + lastName + birthDate üçü de set edildiğinde `true` olur
 * (kayıt/profil tamamlama adımı bu alanı yönetir).
 */
data class User(
    val id: String,
    val phone: String,
    val displayName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val profileCompleted: Boolean = false,
    val membership: Membership? = null,
)

/**
 * Premium üyelik bilgisi (API `Membership` şeması — bkz. `docs/api/openapi.json`).
 *
 * Tier KAYNAĞI API'dir (§2.2 — istemci hesaplamaz/uydurmaz): free hesapta [User.membership]
 * `null`'dır, premium hesapta dolu gelir. Aktiflik [isActive] ile belirlenir; yetki zaten sunucu
 * tarafında zorlanır (ör. `stream-url` free'ye 403), istemci yalnızca aynalar.
 */
data class Membership(
    /** "one-time" | "recurring". */
    val type: String,
    /** "active" | "expired". */
    val status: String,
    val expiresAt: String? = null,
) {
    /** Üyelik aktif mi (premium erişim sürüyor mu). */
    val isActive: Boolean
        get() = status.equals("active", ignoreCase = true)
}

/** Kullanıcı premium mi (aktif üyeliği var mı). */
val User.isPremium: Boolean
    get() = membership?.isActive == true

/**
 * JWT erişim token'ı + opak yenileme (refresh) token'ı.
 *
 * [accessToken] `me` grubu uçlarına Bearer olarak gönderilir (sonraki aşama). [refreshToken]
 * `auth/refresh` ile yeni çift almak için kullanılır ve her kullanımda rotate edilir.
 * [expiresIn] erişim token'ının saniye cinsinden ömrüdür.
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

/**
 * `auth/otp/verify` sonucu: token çifti + (varsa) kullanıcı + kayıt durumu.
 *
 * [firstTime] `true` ise kayıt henüz tamamlanmamıştır; UI aşamasında profil tamamlama
 * adımına yönlendirme bu alana göre yapılır.
 */
data class AuthSession(
    val tokens: AuthTokens,
    val user: User?,
    val firstTime: Boolean,
)

/**
 * `auth/otp/request` sonucu: kodun "gönderilip gönderilmediği" + kayıt durumu.
 *
 * [firstTime] telefon yeni ya da kaydı eksikse `true` döner.
 */
data class OtpRequestResult(
    val sent: Boolean,
    val firstTime: Boolean,
)
