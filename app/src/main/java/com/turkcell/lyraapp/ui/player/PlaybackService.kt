package com.turkcell.lyraapp.ui.player

import android.content.Intent
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.turkcell.lyraapp.data.feed.SongRepository
import dagger.hilt.android.AndroidEntryPoint
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
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var songRepository: SongRepository

    private var mediaSession: MediaSession? = null

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
        val dataSourceFactory = ResolvingDataSource.Factory(DefaultHttpDataSource.Factory(), resolver)

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
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
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
