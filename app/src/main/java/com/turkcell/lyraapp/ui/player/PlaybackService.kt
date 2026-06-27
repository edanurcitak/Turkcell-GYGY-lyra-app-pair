package com.turkcell.lyraapp.ui.player

import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.turkcell.lyraapp.data.feed.SongRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject

/**
 * Arka plan oynatmayı yöneten medya oturumu servisi.
 *
 * [ExoPlayer]'ın sahibi bu servistir; böylece oynatma, Activity/ViewModel yaşam döngüsünden
 * bağımsız olarak uygulama arka plandayken de sürer ve Media3, bağlı [MediaSession]'ın meta
 * verisinden görseldeki sistem medya bildirimini ("mini player") üretir.
 *
 * Kuyruk (önceki/sonraki) [PlayerViewModel] tarafından kurulur; her parça, gerçek stream URL'i
 * yerine `lyra://song/<id>` yer tutucu URI'siyle gelir. Stream URL'leri kısa ömürlü/imzalı
 * olduğundan ([StreamUrl] TTL ~300 sn), URL **tam çalınacağı an** [ResolvingDataSource] ile
 * çözülür — bu yüzden servis [SongRepository]'yi enjekte eder (@AndroidEntryPoint).
 *
 * Bildirim kontrolleri Media3 varsayılanıdır: önceki / oynat-duraklat / sonraki. Talep gereği
 * favori (kalp) butonu eklenmez.
 *
 * **Çalma kaydı:** Player'a sahip tek yer burası olduğundan (tam ekran player, mini player ve
 * bildirimden gelen tüm geçişleri görür), yeni bir parçaya geçildiğinde `POST /me/plays`
 * ([SongRepository.recordPlay]) **parça başına bir kez** çağrılır. Bu, "Son Çalınanlar" ve öneri
 * uçlarını besleyen tek sinyaldir. Range istekleri DataSource düzeyinde olduğundan burada sayılmaz.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var songRepository: SongRepository

    // Çevrimdışı oynatma için paylaşılan medya cache'i (indirme de aynı cache'e yazar; bkz. MediaCacheModule).
    @Inject
    lateinit var mediaCache: SimpleCache

    private var mediaSession: MediaSession? = null

    // Çalma kaydı (ağ) için servis ömrü boyunca yaşayan kapsam; oynatmadan bağımsız, en iyi çaba.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Aynı parçayı (örn. tekrar/yeniden tetik) art arda iki kez kaydetmemek için son kaydedilen id.
    private var lastRecordedSongId: String? = null

    private val playRecorder = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val songId = mediaItem?.mediaId?.takeIf { it.isNotEmpty() } ?: return
            if (songId == lastRecordedSongId) return
            lastRecordedSongId = songId
            serviceScope.launch {
                // En iyi çaba: kayıt başarısızlığı oynatmayı etkilememeli.
                runCatching { songRepository.recordPlay(songId) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        // `lyra://song/<id>` URI'sindeki şarkı id'sini, çalma anında gerçek imzalı URL'e çevirir.
        val resolver = ResolvingDataSource.Resolver { dataSpec ->
            val songId = dataSpec.uri.lastPathSegment.orEmpty()
            val url = try {
                runBlocking { songRepository.getStreamUrl(songId).url }
            } catch (e: Exception) {
                throw IOException("Stream URL alınamadı: $songId", e)
            }
            dataSpec.withUri(Uri.parse(url))
        }
        val upstreamFactory = ResolvingDataSource.Factory(DefaultHttpDataSource.Factory(), resolver)

        // Cache okuma yolu: `songId` cache key'iyle indirilmiş bir şarkı cache'te tam bulunursa
        // CacheDataSource baytları doğrudan cihaz belleğinden verir; upstream (resolver) hiç açılmaz
        // → internete ya da yeni bir stream-url isteğine gerek kalmaz (çevrimdışı çalma).
        // Salt-okunur (write sink null): cache yalnızca bilinçli indirmelerle dolar, akışta şişmez.
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(mediaCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            // Kulaklık çıkarılınca/ses başka uygulamaya geçince duraklat.
            .setHandleAudioBecomingNoisy(true)
            .build()
        // Yeni parçaya her geçişte çalma kaydı için dinleyici (tüm oynatma yollarını kapsar).
        player.addListener(playRecorder)
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    /** Uygulama görevden kaydırılınca: çalmıyorsa servisi kapat, çalıyorsa arka planda sürsün. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.run {
            player.removeListener(playRecorder)
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
