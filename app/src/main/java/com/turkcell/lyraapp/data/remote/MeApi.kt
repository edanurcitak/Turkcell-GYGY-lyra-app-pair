package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.RecordPlayBody
import com.turkcell.lyraapp.data.remote.dto.RecordPlayResponseDto
import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBody
import com.turkcell.lyraapp.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * `me` grubu uç noktaları (bkz. `docs/api/openapi.json` → `me`).
 *
 * [AuthApi]'den farklı olarak bu uçlar **korumalıdır**: `Authorization: Bearer <accessToken>`
 * header'ı zorunludur. Token, paylaşılan OkHttp istemcisine bir interceptor eklemek yerine
 * çağrı başına [Header] ile geçirilir; böylece [AuthApi]'nin "public" sözleşmesi ve ortak
 * istemci yapılandırması bozulmaz. Token, [com.turkcell.lyraapp.data.auth.TokenStore]'dan okunur.
 */
interface MeApi {

    /**
     * Profil bilgisini (ad/soyad/doğum tarihi) ayarlar — `firstTime` kullanıcılar için kayıt adımı.
     *
     * Üç alan da set edildiğinde `profileCompleted` `true` olur ve sonraki `otp/request`
     * çağrıları `firstTime: false` döner.
     */
    @POST("api/v1/me/update-informations")
    suspend fun updateInformations(
        @Header("Authorization") authorization: String,
        @Body body: UpdateInformationsBody,
    ): UserResponseDto

    /**
     * "Son Çalınanlar" — kullanıcının son çaldığı farklı şarkılar (en yeni çalma önce).
     *
     * Yanıt zarfı katalog ile aynı (`{ data: [Song] }`) olduğundan [SongsResponseDto] yeniden
     * kullanılır (`nextCursor` bu uçta gelmez, varsayılan `null` kalır).
     */
    @GET("api/v1/me/recently-played")
    suspend fun getRecentlyPlayed(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
    ): SongsResponseDto

    /**
     * "Senin İçin Müzikler" — kullanıcının en çok çaldığı sanatçılardan kişiselleştirilmiş karışım.
     * Çalma geçmişi yoksa en yeni katalağa düşer.
     */
    @GET("api/v1/me/for-you")
    suspend fun getForYou(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
    ): SongsResponseDto

    /**
     * "Öneriler" — çalınan sanatçıların, henüz çalınmamış diğer şarkıları.
     * Geçmiş yoksa en yeni çalınmamış katalağa düşer.
     */
    @GET("api/v1/me/recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 20,
    ): SongsResponseDto

    /**
     * Gerçek bir çalmayı kaydeder — "Son Çalınanlar" ve öneri uçlarını besleyen tek sinyaldir.
     * Parça başına bir kez çağrılır (Range isteği başına değil).
     */
    @POST("api/v1/me/plays")
    suspend fun recordPlay(
        @Header("Authorization") authorization: String,
        @Body body: RecordPlayBody,
    ): RecordPlayResponseDto
}
