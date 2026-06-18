package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.PlaylistDetailResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistsResponseDto
import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.StreamUrlResponseDto
import retrofit2.http.GET
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
     * Bir şarkı için kısa ömürlü imzalı akış URL'i üretir (oynatmadan hemen önce çağrılır).
     */
    @GET("api/v1/songs/{id}/stream-url")
    suspend fun getStreamUrl(@Path("id") songId: String): StreamUrlResponseDto

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
}
