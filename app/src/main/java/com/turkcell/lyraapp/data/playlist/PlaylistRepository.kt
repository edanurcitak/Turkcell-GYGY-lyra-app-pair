package com.turkcell.lyraapp.data.playlist

import com.turkcell.lyraapp.data.download.DownloadStore
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.remote.StreamingApi
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
     * "İndirilen Şarkılar" listesini yerel indirme deposundan döndürür (ağ kullanmaz).
     * Çevrimdışıyken kütüphanede gösterilen tek liste budur.
     */
    fun getDownloadedPlaylist(): Playlist
}

/**
 * [PlaylistRepository]'nin Streaming API implementasyonu.
 *
 * `GET /api/v1/playlists` listesini çeker; her liste için şarkı sayısını detay ucundan
 * (`GET /api/v1/playlists/{id}`) **paralel** olarak sayar (kullanıcı kararı: gerçek şarkı
 * sayısı; liste ucu sayı vermez → N+1 istek). Listenin başına uygulama düzeyinde özel
 * "Beğenilen Şarkılar" listesi eklenir; sayısı **şimdilik** şarkı API'sinden
 * (`GET /api/v1/songs`) türetilir (kullanıcı kararı, §2.2 — geçici stand-in).
 *
 * Tekil detay/şarkı çağrıları [runCatching] ile sarmalanır: biri başarısız olursa o listenin
 * sayısı 0 kabul edilir, tüm ekran çökmez. Ana `/playlists` çağrısının hatası ise yukarı taşınır.
 */
class ApiPlaylistRepository @Inject constructor(
    private val api: StreamingApi,
    private val downloadStore: DownloadStore,
) : PlaylistRepository {

    override suspend fun getPlaylists(): List<Playlist> = coroutineScope {
        // "Beğenilen Şarkılar" sayısı: şimdilik şarkı API'sinden (en çok bir sayfa / 100).
        val likedCount = async {
            runCatching { api.getSongs(limit = MAX_LIKED_SONGS).data.size }.getOrDefault(0)
        }
        // Gerçek çalma listeleri + her biri için detaydan şarkı sayısı (paralel N+1).
        val playlists = api.getPlaylists().data
            .map { dto ->
                async {
                    val count = runCatching {
                        api.getPlaylistDetail(dto.id).data.songs.size
                    }.getOrDefault(0)
                    dto.toDomain(songCount = count)
                }
            }
            .awaitAll()

        val liked = Playlist(
            id = LIKED_PLAYLIST_ID,
            name = LIKED_PLAYLIST_NAME,
            description = null,
            songCount = likedCount.await(),
            isLiked = true,
            isPinned = true,
        )
        // "İndirilen Şarkılar" daima görünür ve sabitlidir (çevrimdışıyken tek gösterilen liste).
        listOf(liked, getDownloadedPlaylist()) + playlists
    }

    /**
     * Bir çalma listesinin detayını şarkılarıyla döndürür.
     *
     * "Beğenilen Şarkılar" (id = [LIKED_PLAYLIST_ID]) uygulama düzeyinde özel bir listedir ve
     * API ucu yoktur; şarkıları şimdilik şarkı API'sinden çekilir (kullanıcı kararı, §2.2).
     * Diğer listeler `GET /api/v1/playlists/{id}` detay ucundan gelir.
     */
    override suspend fun getPlaylistDetail(playlistId: String): PlaylistDetail =
        when (playlistId) {
            LIKED_PLAYLIST_ID -> {
                val songs: List<Song> = api.getSongs(limit = MAX_LIKED_SONGS).data.map { it.toDomain() }
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

    private companion object {
        const val LIKED_PLAYLIST_ID = "liked"
        const val LIKED_PLAYLIST_NAME = "Beğenilen Şarkılar"
        const val DOWNLOADED_PLAYLIST_ID = "downloaded"
        const val DOWNLOADED_PLAYLIST_NAME = "İndirilen Şarkılar"
        const val MAX_LIKED_SONGS = 100
    }
}
