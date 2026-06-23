package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.AuthSessionResponseDto
import com.turkcell.lyraapp.data.remote.dto.AuthTokensResponseDto
import com.turkcell.lyraapp.data.remote.dto.LogoutBody
import com.turkcell.lyraapp.data.remote.dto.LogoutResponseDto
import com.turkcell.lyraapp.data.remote.dto.OtpRequestBody
import com.turkcell.lyraapp.data.remote.dto.OtpRequestResponseDto
import com.turkcell.lyraapp.data.remote.dto.OtpVerifyBody
import com.turkcell.lyraapp.data.remote.dto.RefreshBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Parolasız (OTP) kimlik doğrulama uç noktaları (bkz. `docs/api/openapi.json` → `auth`).
 *
 * Tüm uçlar public'tir (Bearer header gerekmez); bu yüzden mevcut [StreamingApi] ile aynı
 * Retrofit/OkHttp istemcisini kullanır. `me` grubu uçları ve token enjekte eden interceptor
 * sonraki aşamanın kapsamındadır.
 */
interface AuthApi {

    /**
     * Bir telefon numarası için OTP "gönderir" (mock — gerçek SMS yok).
     *
     * Yanıttaki `firstTime`, kaydın tamamlanıp tamamlanmadığını bildirir.
     */
    @POST("api/v1/auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): OtpRequestResponseDto

    /**
     * OTP'yi doğrular ve erişim + yenileme token'ı döndürür.
     *
     * İlk girişte kullanıcı otomatik oluşturulur (passwordless signup).
     */
    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): AuthSessionResponseDto

    /**
     * Yenileme token'ını yeni bir token çiftiyle değiştirir (rotation).
     *
     * Sunulan token iptal edilir; iptal/expired/bilinmeyen token 401 döner.
     */
    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): AuthTokensResponseDto

    /**
     * Verilen yenileme token'ını iptal eder (idempotent).
     */
    @POST("api/v1/auth/logout")
    suspend fun logout(@Body body: LogoutBody): LogoutResponseDto
}
