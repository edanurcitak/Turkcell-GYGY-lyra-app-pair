package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.CheckoutBody
import com.turkcell.lyraapp.data.remote.dto.CheckoutResponseDto
import com.turkcell.lyraapp.data.remote.dto.MembershipPlansResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistDetailResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistsResponseDto
import com.turkcell.lyraapp.data.remote.dto.SongResponseDto
import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.StreamUrlResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Streaming API uç noktaları (bkz. `docs/api/openapi.json`).
 *
 * Şarkı listeleme ve çalma listeleri kullanılır; `stream-url` (çalma) sonraki adımların
 * kapsamındadır.
 */
interface StreamingApi {

    /**
     * Şarkı kataloğunu sayfalı döndürür.
     *
     * @param limit sayfa boyutu (1–100, API varsayılanı 20)
     * @param cursor önceki yanıttaki `nextCursor`; ilk sayfada null
     * @param query başlık/sanatçı/albümde arama (`q`)
     */
    @GET("api/v1/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
        @Query("q") query: String? = null,
    ): SongsResponseDto

    /**
     * Tek bir şarkının detayını id ile döndürür.
     *
     * **Public** — auth gerektirmez (bkz. `docs/api/openapi.json` → `songs`); katalog listesiyle aynı
     * `Song` şemasını taşıdığından [SongResponseDto] tekil zarfı kullanılır.
     */
    @GET("api/v1/songs/{id}")
    suspend fun getSong(@Path("id") id: String): SongResponseDto

    /**
     * Bir şarkı için kısa ömürlü imzalı akış URL'i üretir (oynatmadan hemen önce çağrılır).
     *
     * **Premium-only ve korumalı** (bkz. `docs/api/openapi.json`): `Authorization: Bearer` zorunlu;
     * free hesap **403** alır (free akış `me/playback/next` üzerinden gider). Token
     * [com.turkcell.lyraapp.data.auth.TokenStore]'dan okunur, çağrı başına [Header] ile geçirilir.
     */
    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(
        @Header("Authorization") authorization: String,
        @Path("id") songId: String,
    ): StreamUrlResponseDto

    /**
     * Tüm çalma listelerini (şarkısız) döndürür.
     */
    @GET("api/v1/playlists")
    suspend fun getPlaylists(): PlaylistsResponseDto

    /**
     * Bir çalma listesinin detayını sıralı şarkılarıyla döndürür.
     *
     * @param id çalma listesi kimliği (ör. `p_late-night-drive`)
     */
    @GET("api/v1/playlists/{id}")
    suspend fun getPlaylistDetail(@Path("id") id: String): PlaylistDetailResponseDto

    /**
     * Premium fiyat kataloğunu (satın alınabilir planlar) döndürür.
     *
     * **Public** — auth gerektirmez (bkz. `docs/api/openapi.json` → `memberships`); diğer public
     * uçlar gibi token'sız çağrılır. Satın alma (`memberships/checkout`) korumalıdır ve kapsam dışıdır.
     */
    @GET("api/v1/memberships/plans")
    suspend fun getMembershipPlans(): MembershipPlansResponseDto

    /**
     * Premium üyelik satın alır (mock kart ödemesi) ve onayda aktif üyeliği döndürür.
     *
     * **Korumalı**: `Authorization: Bearer` zorunlu (bkz. `docs/api/openapi.json` →
     * `memberships/checkout`). Token [com.turkcell.lyraapp.data.auth.TokenStore]'dan okunur ve
     * [getStreamUrl] gibi çağrı başına [Header] ile geçirilir. Sonuç kart numarasına göre
     * belirlenir: onay → 201, red → 402.
     */
    @POST("api/v1/memberships/checkout")
    suspend fun checkout(
        @Header("Authorization") authorization: String,
        @Body body: CheckoutBody,
    ): CheckoutResponseDto
}
