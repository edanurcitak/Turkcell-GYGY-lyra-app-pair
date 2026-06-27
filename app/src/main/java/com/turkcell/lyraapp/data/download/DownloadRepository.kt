package com.turkcell.lyraapp.data.download

import android.net.Uri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.SimpleCache
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.data.membership.MembershipStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Free hesap, premium'a özel çevrimdışı indirmeyi denediğinde fırlatılır.
 *
 * İki yerde oluşur: (1) istemci tier kontrolü ([MembershipStore.isPremium] false) — ağ isteği bile
 * yapılmaz; (2) savunma amaçlı, sunucu `stream-url`'e **403** dönerse (tier'ın nihai otoritesi
 * sunucudur). UI bunu "Premium gerekli" ipucuna çevirir.
 */
class PremiumRequiredException : Exception("Çevrimdışı indirme Premium üyelere özeldir.")

/**
 * İndirme verisinin domain arayüzü (SongRepository/PlaylistRepository ile aynı kalıp).
 *
 * Şu an tek implementasyon, şarkıyı Media3 cache'ine yazan [MediaDownloadRepository]'dir.
 *
 * Not (§2.1): Arayüz ve tek implementasyonu, batch'in 5 dosya sınırını aşmamak için aynı
 * dosyada tutuldu.
 */
interface DownloadRepository {

    /** Şarkının tamamını cihaz belleğine (cache) indirir ve meta verisini [DownloadStore]'a yazar. */
    suspend fun download(song: Song)

    suspend fun remove(songId: String)

    fun isDownloaded(songId: String): Boolean
}

/**
 * Şarkıyı [SimpleCache]'e tam olarak yazan implementasyon.
 *
 * Akış: imzalı stream URL'i [SongRepository.getStreamUrl] ile çözülür → `songId` cache key'iyle bir
 * [DataSpec] kurulur → [CacheWriter] kaynağın tamamını cache'e yazar → meta veri [DownloadStore]'a
 * eklenir. Cache key olarak oynatma ile **aynı** `songId` kullanıldığından (bkz.
 * [com.turkcell.lyraapp.ui.player.PlayerViewModel] `setCustomCacheKey`), oynatma sırasında
 * [CacheDataSource] dosyayı bellekte bulur ve internete/yeni API isteğine gerek kalmaz.
 *
 * İmzalı URL kısa ömürlüdür ama bu sorun değil: indirme anında geçerlidir; sonradan oynatmada
 * URL'e değil cache'teki baytlara bakılır.
 */
class MediaDownloadRepository @Inject constructor(
    private val cache: SimpleCache,
    private val songRepository: SongRepository,
    private val downloadStore: DownloadStore,
    private val membershipStore: MembershipStore,
) : DownloadRepository {

    override suspend fun download(song: Song): Unit = withContext(Dispatchers.IO) {
        // Tier kapısı: çevrimdışı indirme premium'a özel (tier kaynağı API; bkz. MembershipStore).
        if (!membershipStore.isPremium) throw PremiumRequiredException()
        if (downloadStore.isDownloaded(song.id)) return@withContext

        // Savunma: sunucu tier'ın nihai otoritesi — free için stream-url 403 döner.
        val streamUrl = try {
            songRepository.getStreamUrl(song.id).url
        } catch (e: HttpException) {
            if (e.code() == 403) throw PremiumRequiredException() else throw e
        }
        val dataSpec = DataSpec.Builder()
            .setUri(Uri.parse(streamUrl))
            // Cache key = songId → oynatma ile aynı anahtar; imzalı URL değişse de eşleşir.
            .setKey(song.id)
            .build()

        val cacheDataSource = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .createDataSource()

        // Tüm kaynağı senkron olarak cache'e yazar (IO dispatcher'da); hata IOException olarak yükselir.
        CacheWriter(
            cacheDataSource,
            dataSpec,
            /* temporaryBuffer = */ null,
            /* progressListener = */ null,
        ).cache()

        downloadStore.add(song)
    }

    override suspend fun remove(songId: String): Unit = withContext(Dispatchers.IO) {
        downloadStore.remove(songId)
    }

    override fun isDownloaded(songId: String): Boolean =
        downloadStore.isDownloaded(songId)
}
