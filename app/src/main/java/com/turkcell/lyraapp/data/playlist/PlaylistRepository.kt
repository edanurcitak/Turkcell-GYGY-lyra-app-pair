package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.auth.TokenStore
import com.turkcell.lyraapp.data.download.DownloadStore
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.StreamingApi
import com.turkcell.lyraapp.data.remote.dto.AddTrackBody
import com.turkcell.lyraapp.data.remote.dto.CreatePlaylistBody
import com.turkcell.lyraapp.data.remote.dto.PlaylistDto
import com.turkcell.lyraapp.data.remote.dto.toDomain
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Çalma listesi verisinin domain arayüzü (SongRepository ile aynı kalıp).
 *
 * Uygulamanın kendi sözleşmesidir; veri kaynağından bağımsızdır. Şu an tek implementasyon
 * gerçek API'dir ([ApiPlaylistRepository]).
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı
 * dosyada tutuldu.
 */
interface PlaylistRepository {

    suspend fun getPlaylists(): List<Playlist>

    suspend fun getPlaylistDetail(playlistId: String): PlaylistDetail

    /**
     * Yeni bir kullanıcı çalma listesi oluşturur — `POST /api/v1/me/playlists` (korumalı).
     * Oluşturulan liste (henüz şarkısız → `songCount = 0`) döner; id'siyle şarkı eklenebilir.
     */
    suspend fun createPlaylist(name: String, description: String? = null): Playlist

    /**
     * Kullanıcının kendi çalma listesini (ve tüm parçalarını) siler —
     * `DELETE /api/v1/me/playlists/{id}` (korumalı; çağıran sahibi olmalı).
     */
    suspend fun deletePlaylist(playlistId: String)

    /**
     * Çalma listesine şarkı ekler — `POST /api/v1/me/playlists/{id}/tracks` (korumalı).
     * Şarkı zaten listedeyse API **409** döner (genel yönetimde yutulmaz; çağıran karar verir).
     */
    suspend fun addSong(playlistId: String, songId: String)

    /** Çalma listesinden şarkı çıkarır — `DELETE /api/v1/me/playlists/{id}/tracks/{songId}` (korumalı). */
    suspend fun removeSong(playlistId: String, songId: String)

    /**
     * "İndirilen Şarkılar" listesini yerel indirme deposundan döndürür (ağ kullanmaz).
     * Çevrimdışıyken kütüphanede gösterilen tek liste budur.
     */
    fun getDownloadedPlaylist(): Playlist

    companion object {
        /**
         * Uygulama düzeyinde "Beğenilen Şarkılar" listesinin id'si (gerçek API id'si değil; favoriler
         * [com.turkcell.lyraapp.data.favorites.FavoritesRepository] ile temsil edilir). Detay ekranı
         * bu id ile liked listeyi tanıyıp favori değişimlerine reaktif tepki verir.
         */
        const val LIKED_PLAYLIST_ID = "liked"
    }
}

/**
 * [PlaylistRepository]'nin Streaming API implementasyonu.
 *
 * `GET /api/v1/playlists` listesini çeker; her liste için şarkı sayısını detay ucundan
 * (`GET /api/v1/playlists/{id}`) **paralel** olarak sayar (kullanıcı kararı: gerçek şarkı
 * sayısı; liste ucu sayı vermez → N+1 istek). Listenin başına uygulama düzeyinde özel
 * "Beğenilen Şarkılar" listesi eklenir; içeriği/sayısı kullanıcının favori çalma listesinden
 * ([FavoritesRepository]) gerçek-zamanlı gelir (dinamik beğen/çıkar; §2.2 stand-in kaldırıldı).
 *
 * Tekil detay/şarkı çağrıları [runCatching] ile sarmalanır: biri başarısız olursa o listenin
 * sayısı 0 kabul edilir, tüm ekran çökmez. Ana `/playlists` çağrısının hatası ise yukarı taşınır.
 */
class ApiPlaylistRepository @Inject constructor(
    private val api: StreamingApi,
    private val meApi: MeApi,
    private val tokenStore: TokenStore,
    private val downloadStore: DownloadStore,
    private val favoritesRepository: FavoritesRepository,
) : PlaylistRepository {

    override suspend fun getPlaylists(): List<Playlist> = coroutineScope {
        // "Beğenilen Şarkılar" sayısı: kullanıcının favori listesinden (gerçek, dinamik).
        val likedCount = async {
            runCatching { favoritesRepository.likedCount() }.getOrDefault(0)
        }
        // Öne çıkan (public) listeler + her biri için detaydan şarkı sayısı (paralel N+1).
        val featured = async { withCounts(api.getPlaylists().data, isOwned = false) }
        // Kullanıcının kendi (owned) listeleri; "Beğenilen Şarkılar" sabitlenmiş kartla çift olmasın
        // diye ada göre elenir (favoriler ayrı mekanizmayla temsil edilir). Oturum/istek başarısızsa boş.
        val owned = async {
            val mine = runCatching { meApi.getMyPlaylists(bearer()).data }.getOrDefault(emptyList())
                .filter { it.name != LIKED_PLAYLIST_NAME }
            withCounts(mine, isOwned = true)
        }

        val liked = Playlist(
            id = LIKED_PLAYLIST_ID,
            name = LIKED_PLAYLIST_NAME,
            description = null,
            songCount = likedCount.await(),
            isLiked = true,
            isPinned = true,
        )
        // "İndirilen Şarkılar" yalnızca görünür indirme varken (premium + dolu) eklenir; free'de ya da
        // indirme yokken gizlenir ([DownloadStore]'un tier/kullanıcı kapısı). "Beğenilen Şarkılar" daima kalır.
        val downloaded = getDownloadedPlaylist()
        val pinned = if (downloaded.songCount > 0) listOf(liked, downloaded) else listOf(liked)
        // Sabitlenenler + kullanıcının kendi listeleri (owned) + öne çıkanlar (public).
        pinned + owned.await() + featured.await()
    }

    /**
     * Bir çalma listesi DTO listesini domain [Playlist]'e çevirir; şarkı sayısını her biri için detay
     * ucundan (`/playlists/{id}`) **paralel** sayar (liste ucu sayı vermez → N+1). [isOwned] tüm
     * sonuçlara işlenir (kaynak `me/playlists` → owned, public → değil).
     */
    private suspend fun withCounts(dtos: List<PlaylistDto>, isOwned: Boolean): List<Playlist> =
        coroutineScope {
            dtos.map { dto ->
                async {
                    val count = runCatching {
                        api.getPlaylistDetail(dto.id).data.songs.size
                    }.getOrDefault(0)
                    dto.toDomain(songCount = count, isOwned = isOwned)
                }
            }.awaitAll()
        }

    /**
     * Bir çalma listesinin detayını şarkılarıyla döndürür.
     *
     * "Beğenilen Şarkılar" (id = [LIKED_PLAYLIST_ID]) uygulama düzeyinde özel bir listedir; şarkıları
     * kullanıcının favori çalma listesinden ([FavoritesRepository]) gerçek-zamanlı gelir (dinamik).
     * Diğer listeler `GET /api/v1/playlists/{id}` detay ucundan gelir.
     */
    override suspend fun getPlaylistDetail(playlistId: String): PlaylistDetail =
        when (playlistId) {
            LIKED_PLAYLIST_ID -> {
                val songs: List<Song> = favoritesRepository.getLikedSongs()
                PlaylistDetail(
                    id = LIKED_PLAYLIST_ID,
                    name = LIKED_PLAYLIST_NAME,
                    description = null,
                    songs = songs,
                )
            }
            // İndirilenler tamamen yereldir (çevrimdışı erişilir); ağ uçlarına dokunmaz.
            DOWNLOADED_PLAYLIST_ID -> PlaylistDetail(
                id = DOWNLOADED_PLAYLIST_ID,
                name = DOWNLOADED_PLAYLIST_NAME,
                description = null,
                songs = downloadStore.downloads.value,
            )
            else -> api.getPlaylistDetail(playlistId).data.toDomain()
        }

    override fun getDownloadedPlaylist(): Playlist = Playlist(
        id = DOWNLOADED_PLAYLIST_ID,
        name = DOWNLOADED_PLAYLIST_NAME,
        description = null,
        songCount = downloadStore.downloads.value.size,
        isLiked = false,
        isPinned = true,
    )

    // --- Genel çalma listesi yönetimi (korumalı `me/playlists` uçları; Bearer token ile) ---

    override suspend fun createPlaylist(name: String, description: String?): Playlist =
        // Yeni liste henüz şarkısız → songCount = 0 (liste ucu sayı vermez, detay çağrısına gerek yok).
        meApi.createPlaylist(bearer(), CreatePlaylistBody(name = name, description = description))
            .data.toDomain(songCount = 0)

    override suspend fun deletePlaylist(playlistId: String) {
        meApi.deletePlaylist(bearer(), playlistId)
    }

    override suspend fun addSong(playlistId: String, songId: String) {
        meApi.addTrack(bearer(), playlistId, AddTrackBody(songId))
    }

    override suspend fun removeSong(playlistId: String, songId: String) {
        meApi.removeTrack(bearer(), playlistId, songId)
    }

    /** Korumalı `me` çağrıları için `Authorization` header değeri; oturum yoksa hata fırlatır. */
    private fun bearer(): String {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Oturum yok (erişim token'ı bulunamadı).")
        return "Bearer $token"
    }

    private companion object {
        const val LIKED_PLAYLIST_ID = PlaylistRepository.LIKED_PLAYLIST_ID
        const val LIKED_PLAYLIST_NAME = "Beğenilen Şarkılar"
        const val DOWNLOADED_PLAYLIST_ID = "downloaded"
        const val DOWNLOADED_PLAYLIST_NAME = "İndirilen Şarkılar"
    }
}
