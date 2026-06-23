package com.turkcell.lyraapp.data.auth

import com.turkcell.lyraapp.data.remote.AuthApi
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.dto.LogoutBody
import com.turkcell.lyraapp.data.remote.dto.OtpRequestBody
import com.turkcell.lyraapp.data.remote.dto.OtpVerifyBody
import com.turkcell.lyraapp.data.remote.dto.RefreshBody
import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBody
import com.turkcell.lyraapp.data.remote.dto.toDomain
import javax.inject.Inject

/**
 * Parolasız (OTP) kimlik doğrulamanın domain arayüzü.
 *
 * Uygulamanın kimlik/kayıt sözleşmesidir; veri kaynağından bağımsızdır. Telefon → OTP →
 * (gerekiyorsa) profil tamamlama akışının tamamını kapsar.
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı
 * dosyada tutuldu ([com.turkcell.lyraapp.data.feed.SongRepository] deseni).
 */
interface OtpAuthRepository {

    /** Telefon için OTP ister; sonuç [OtpRequestResult.firstTime] ile kayıt durumunu bildirir. */
    suspend fun requestOtp(phone: String): Result<OtpRequestResult>

    /** OTP'yi doğrular, token çiftini saklar ve oturumu döndürür. */
    suspend fun verifyOtp(phone: String, code: String): Result<AuthSession>

    /**
     * `firstTime` kullanıcı için profil tamamlama (kayıt) adımı: ad/soyad/doğum tarihini ayarlar.
     *
     * Saklı erişim token'ını kullanır (önce [verifyOtp] gerekir). [birthDate] `YYYY-MM-DD` biçiminde.
     */
    suspend fun updateInformations(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<User>

    /** Saklı yenileme token'ıyla yeni bir token çifti alır (rotation). */
    suspend fun refresh(): Result<AuthTokens>

    /** Saklı yenileme token'ını iptal eder ve yerel oturumu temizler. */
    suspend fun logout(): Result<Unit>
}

/**
 * [OtpAuthRepository]'nin Streaming API implementasyonu.
 *
 * Ağ çağrıları [runCatching] ile sarılır → çağıran (ViewModel) `Result` üzerinden başarı/başarısızlığı
 * işler. Başarılı `verifyOtp`/`refresh` token çiftini [TokenStore]'a yazar; `logout` temizler.
 */
class ApiOtpAuthRepository @Inject constructor(
    private val api: AuthApi,
    private val meApi: MeApi,
    private val tokenStore: TokenStore,
) : OtpAuthRepository {

    override suspend fun requestOtp(phone: String): Result<OtpRequestResult> = runCatching {
        val data = api.requestOtp(OtpRequestBody(phone = phone)).data
        OtpRequestResult(sent = data.sent, firstTime = data.firstTime)
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<AuthSession> = runCatching {
        val session = api.verifyOtp(OtpVerifyBody(phone = phone, code = code)).data.toDomain()
        tokenStore.save(
            accessToken = session.tokens.accessToken,
            refreshToken = session.tokens.refreshToken,
        )
        session
    }

    override suspend fun updateInformations(
        firstName: String,
        lastName: String,
        birthDate: String,
    ): Result<User> = runCatching {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Profil için oturum yok (erişim token'ı bulunamadı).")
        meApi.updateInformations(
            authorization = "Bearer $token",
            body = UpdateInformationsBody(
                firstName = firstName,
                lastName = lastName,
                birthDate = birthDate,
            ),
        ).data.toDomain()
    }

    override suspend fun refresh(): Result<AuthTokens> = runCatching {
        val current = tokenStore.refreshToken
            ?: throw IllegalStateException("Yenilenecek oturum yok (refresh token bulunamadı).")
        val tokens = api.refresh(RefreshBody(refreshToken = current)).data.toDomain()
        tokenStore.save(accessToken = tokens.accessToken, refreshToken = tokens.refreshToken)
        tokens
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        // Sunucuda iptal et (varsa); idempotent olduğundan bilinmeyen token da sorun değil.
        tokenStore.refreshToken?.let { api.logout(LogoutBody(refreshToken = it)) }
        tokenStore.clear()
    }
}
