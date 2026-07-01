package com.turkcell.lyraapp.data.favorites

import com.turkcell.lyraapp.data.auth.TokenStore
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.remote.MeApi
import com.turkcell.lyraapp.data.remote.StreamingApi
import com.turkcell.lyraapp.data.remote.dto.AddTrackBody
import com.turkcell.lyraapp.data.remote.dto.CreatePlaylistBody
import com.turkcell.lyraapp.data.remote.dto.toDomain
import kotlinx.coroutines.flow.StateFlow
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Favori ("beğenilen") şarkı verisinin domain arayüzü (SongRepository/PlaylistRepository ile aynı kalıp).
 *
 * API'da ayrı bir favori ucu yoktur; favoriler "Beğenilen Şarkılar" adlı bir kullanıcı çalma
 * listesiyle temsil edilir (kullanıcı kararı, §2.2). Bu arayüz beğen/beğeniyi-kaldır ve okuma
 * işlemlerini o listenin yönetimine çevirir.
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı dosyada tutuldu.
 */
interface FavoritesRepository {

    /** Beğenilen şarkı id'lerinin sıcak akışı (UI'ın kalp durumunu sürmesi için; kaynak [FavoritesStore]). */
    val likedIds: StateFlow<Set<String>>

    /** Şarkı şu an beğenili mi (önbellekten anlık). */
    fun isLiked(songId: String): Boolean

    /** "Beğenilen Şarkılar" listesini API'den çözüp id önbelleğini tazeler (Player açılışı vb.). */
    suspend fun refresh()

    /** Beğenilen şarkıları (tam [Song]) döndürür; önbelleği de tazeler (Kütüphane detayı için). */
    suspend fun getLikedSongs(): List<Song>

    /** Beğenilen şarkı sayısı (Kütüphane liste kartı için). */
    suspend fun likedCount(): Int

    /** Şarkıyı beğenir: liste yoksa oluşturur, track ekler (409 = zaten beğenili → başarı sayılır). */
    suspend fun add(songId: String)

    /** Şarkının beğenisini kaldırır: listeden track siler. */
    suspend fun remove(songId: String)
}

/**
 * [FavoritesRepository]'nin API implementasyonu.
 *
 * "Beğenilen Şarkılar" kullanıcı çalma listesini `GET /me/playlists` içinden **ada göre** çözer
 * ([LIKED_PLAYLIST_NAME]); ilk beğenide liste yoksa `POST /me/playlists` ile oluşturur. Beğen/çıkar,
 * `me/playlists/{id}/tracks` uçlarına gider. Liste **şarkılarını** okumak için tek yol olan public
 * `GET /playlists/{id}` detay ucu kullanılır (§2.2 — kullanıcı listesinin id'yle okunabildiği varsayımı).
 *
 * Korumalı `me` çağrıları için Bearer token [TokenStore]'dan okunup [MeApi]'ye `@Header` ile geçirilir
 * (mevcut [com.turkcell.lyraapp.data.feed.ApiSongRepository] deseni). Çözümlenen liste id'si
 * [FavoritesStore]'da önbelleğe alınır.
 */
class ApiFavoritesRepository @Inject constructor(
    private val meApi: MeApi,
    private val streamingApi: StreamingApi,
    private val tokenStore: TokenStore,
    private val store: FavoritesStore,
) : FavoritesRepository {

    override val likedIds: StateFlow<Set<String>> get() = store.likedIds

    override fun isLiked(songId: String): Boolean = store.isLiked(songId)

    override suspend fun refresh() {
        store.setAll(loadLikedSongs().map { it.id })
    }

    override suspend fun getLikedSongs(): List<Song> {
        val songs = loadLikedSongs()
        store.setAll(songs.map { it.id })
        return songs
    }

    override suspend fun likedCount(): Int = getLikedSongs().size

    override suspend fun add(songId: String) {
        val playlistId = ensureLikedPlaylist()
        try {
            meApi.addTrack(bearer(), playlistId, AddTrackBody(songId))
        } catch (e: HttpException) {
            // 409 = şarkı zaten listede: hedef durum zaten sağlandı, başarı say.
            if (e.code() != HTTP_CONFLICT) throw e
        }
        store.add(songId)
    }

    override suspend fun remove(songId: String) {
        // Liste yoksa kaldırılacak bir şey de yok.
        val playlistId = resolveLikedPlaylistId() ?: return
        meApi.removeTrack(bearer(), playlistId, songId)
        store.remove(songId)
    }

    /** "Beğenilen Şarkılar" listesini çözer ve şarkılarını okur; liste yoksa boş döner. */
    private suspend fun loadLikedSongs(): List<Song> {
        val playlistId = resolveLikedPlaylistId() ?: return emptyList()
        // Detay okuması başarısızsa (ör. liste yeni silinmiş) tüm ekranı çökertme; boş kabul et.
        return runCatching {
            streamingApi.getPlaylistDetail(playlistId).data.songs.map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    /** me/playlists içinden adı [LIKED_PLAYLIST_NAME] olan listenin id'sini bulur; önbelleğe alır. */
    private suspend fun resolveLikedPlaylistId(): String? {
        store.likedPlaylistId?.let { return it }
        val id = meApi.getMyPlaylists(bearer()).data
            .firstOrNull { it.name == LIKED_PLAYLIST_NAME }
            ?.id
        store.likedPlaylistId = id
        return id
    }

    /** Var olan "Beğenilen Şarkılar" listesini döndürür; yoksa oluşturup id'yi önbelleğe alır. */
    private suspend fun ensureLikedPlaylist(): String {
        resolveLikedPlaylistId()?.let { return it }
        val created = meApi.createPlaylist(bearer(), CreatePlaylistBody(name = LIKED_PLAYLIST_NAME)).data
        store.likedPlaylistId = created.id
        return created.id
    }

    /** Korumalı `me` çağrıları için `Authorization` header değeri; oturum yoksa hata fırlatır. */
    private fun bearer(): String {
        val token = tokenStore.accessToken
            ?: throw IllegalStateException("Oturum yok (erişim token'ı bulunamadı).")
        return "Bearer $token"
    }

    companion object {
        /** Favorileri temsil eden kullanıcı çalma listesinin adı (Kütüphane'deki pinli listeyle aynı). */
        const val LIKED_PLAYLIST_NAME = "Beğenilen Şarkılar"
        private const val HTTP_CONFLICT = 409
    }
}
