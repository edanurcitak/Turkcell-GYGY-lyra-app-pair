package com.turkcell.lyraapp.data.remote

import com.turkcell.lyraapp.data.remote.dto.AddTrackBody
import com.turkcell.lyraapp.data.remote.dto.AddTrackResponseDto
import com.turkcell.lyraapp.data.remote.dto.AdCompleteBody
import com.turkcell.lyraapp.data.remote.dto.AdCompleteResponseDto
import com.turkcell.lyraapp.data.remote.dto.CreatePlaylistBody
import com.turkcell.lyraapp.data.remote.dto.DeletePlaylistResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextBody
import com.turkcell.lyraapp.data.remote.dto.PlaybackNextResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistResponseDto
import com.turkcell.lyraapp.data.remote.dto.PlaylistsResponseDto
import com.turkcell.lyraapp.data.remote.dto.RecordPlayBody
import com.turkcell.lyraapp.data.remote.dto.RecordPlayResponseDto
import com.turkcell.lyraapp.data.remote.dto.RemoveTrackResponseDto
import com.turkcell.lyraapp.data.remote.dto.SongsResponseDto
import com.turkcell.lyraapp.data.remote.dto.UpdateInformationsBody
import com.turkcell.lyraapp.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
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
     * Oturum açan kullanıcının güncel profilini döndürür (kimlik + aktif üyelik/tier).
     *
     * Yanıt zarfı login (`/auth/otp/verify`) ile aynı `User` şemasını taşıdığından [UserResponseDto]
     * yeniden kullanılır; istemci profili/tier'ı sunucudan tazelemek için çağırır (§2.2 — hesaplamaz).
     */
    @GET("api/v1/me")
    suspend fun getMe(
        @Header("Authorization") authorization: String,
    ): UserResponseDto

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

    /**
     * Free akış için "sıradaki ne çalınmalı?" — şarkı ya da (3'te 1) önce reklam döndürür.
     *
     * Bu uç çalmayı **kendisi kaydeder**; bu nedenle bunu kullanan istemci ayrıca [recordPlay]
     * çağırmaz. Premium hesaplar bunun yerine doğrudan `songs/{id}/stream-url` kullanır.
     */
    @POST("api/v1/me/playback/next")
    suspend fun playbackNext(
        @Header("Authorization") authorization: String,
        @Body body: PlaybackNextBody,
    ): PlaybackNextResponseDto

    /**
     * Sunulan reklamın tamamlandığını bildirir (analytics). `type: "ad"` yanıtındaki
     * `impressionId` geçirilir.
     */
    @POST("api/v1/me/playback/ad-complete")
    suspend fun adComplete(
        @Header("Authorization") authorization: String,
        @Body body: AdCompleteBody,
    ): AdCompleteResponseDto

    // --- Kullanıcı çalma listeleri (favoriler "Beğenilen Şarkılar" listesiyle temsil edilir) ---

    /**
     * Kullanıcının kendi çalma listelerini (şarkısız) döndürür.
     *
     * Katalog listesiyle aynı zarfı (`{ data: [Playlist] }`) döndürdüğünden [PlaylistsResponseDto]
     * yeniden kullanılır. "Beğenilen Şarkılar" listesi bu uçtan ada göre bulunur/çözülür.
     */
    @GET("api/v1/me/playlists")
    suspend fun getMyPlaylists(
        @Header("Authorization") authorization: String,
    ): PlaylistsResponseDto

    /** Yeni bir kullanıcı çalma listesi oluşturur (ilk beğenide "Beğenilen Şarkılar" için). */
    @POST("api/v1/me/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") authorization: String,
        @Body body: CreatePlaylistBody,
    ): PlaylistResponseDto

    /**
     * Kullanıcının kendi çalma listesini (ve tüm parçalarını) siler; çağıran sahibi olmalı.
     *
     * Sahip olmayan çağrı **403**, olmayan liste **404** döner (bkz. `docs/api/openapi.json`).
     */
    @DELETE("api/v1/me/playlists/{id}")
    suspend fun deletePlaylist(
        @Header("Authorization") authorization: String,
        @Path("id") playlistId: String,
    ): DeletePlaylistResponseDto

    /**
     * Bir şarkıyı kullanıcının çalma listesine ekler ("beğen").
     *
     * Şarkı zaten listedeyse API **409** döner; çağıran bunu "zaten beğenili" olarak tolere eder.
     */
    @POST("api/v1/me/playlists/{id}/tracks")
    suspend fun addTrack(
        @Header("Authorization") authorization: String,
        @Path("id") playlistId: String,
        @Body body: AddTrackBody,
    ): AddTrackResponseDto

    /** Bir şarkıyı kullanıcının çalma listesinden kaldırır ("beğeniyi kaldır"). */
    @DELETE("api/v1/me/playlists/{id}/tracks/{songId}")
    suspend fun removeTrack(
        @Header("Authorization") authorization: String,
        @Path("id") playlistId: String,
        @Path("songId") songId: String,
    ): RemoveTrackResponseDto
}
