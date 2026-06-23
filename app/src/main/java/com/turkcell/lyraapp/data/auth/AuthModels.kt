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
)

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
