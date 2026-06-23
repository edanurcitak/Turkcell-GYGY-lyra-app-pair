package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBody
import com.turkcell.lyraapp.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

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
}
