package com.turkcell.lyraapp.ui.player

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.feed.StreamUrl
import com.turkcell.lyraapp.data.playback.AdInfo
import com.turkcell.lyraapp.data.playback.PlaybackRepository
import com.turkcell.lyraapp.data.playback.PlaybackResolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * Free hesapların oynatma orkestrasyonu — [PlaybackService] içinde yaşar, böylece arka planda da
 * sürer (premium ile eşdeğer).
 *
 * Free akışta her şarkı sunucu-otoriteli `playback/next` ([PlaybackRepository.resolveNext]) ile
 * çözülür: doğrudan şarkı ya da önce reklam. Çözülen **gerçek** URL'ler tek seferlik bir kuyruğa
 * (`[reklam, şarkı]` veya `[şarkı]`) konur — `lyra://` yer tutucu kullanılmaz, dolayısıyla
 * premium-only `stream-url` resolver'ı (403) devreye girmez.
 *
 * Mantıksal kuyruk tek şarkı olduğundan, önceki/sonraki ve **bildirim** butonlarının her yerden
 * çalışması için ExoPlayer bir [ForwardingPlayer] ([player]) ile sarılır: `seekToNext/Previous`
 * free ilerlemeye yönlendirilir, ilgili komutlar [getAvailableCommands] ile yayınlanır. Şarkı
 * bitince ([Player.STATE_ENDED]) bir sonrakine otomatik geçilir. Reklam→şarkı geçişinde
 * `ad-complete` çağrılır. Çalma kaydı sunucuda (`playback/next`) tutulduğundan servis `/me/plays`
 * çağırmaz (bkz. [PlaybackService]).
 */
class FreePlaybackController(
    private val exoPlayer: ExoPlayer,
    private val playbackRepository: PlaybackRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var freeMode = false
    private var tracks: List<FreeTrack> = emptyList()
    private var index = 0
    private var pendingImpressionId: String? = null
    private var resolveJob: Job? = null

    private val hasNext: Boolean get() = freeMode && index < tracks.lastIndex
    private val hasPrevious: Boolean get() = freeMode && index > 0
    private val currentIsAd: Boolean
        get() = exoPlayer.currentMediaItem?.mediaId?.startsWith(PlaybackCommands.AD_MEDIA_ID_PREFIX) == true

    /**
     * Session'a verilen oynatıcı. Free modda next/prev'i serbest ilerlemeye yönlendirir; premium
     * modda ([freeMode] false) her şeyi olduğu gibi alttaki ExoPlayer'a iletir (davranış değişmez).
     */
    val player: Player = object : ForwardingPlayer(exoPlayer) {
        override fun getAvailableCommands(): Player.Commands {
            val base = super.getAvailableCommands()
            if (!freeMode || tracks.size <= 1) return base
            // Tek parçalık kuyrukta bile next/prev'i yayınla ki kontrolcüler/bildirim butonu göstersin.
            return base.buildUpon()
                .addAll(
                    COMMAND_SEEK_TO_NEXT,
                    COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
                    COMMAND_SEEK_TO_PREVIOUS,
                    COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
                )
                .build()
        }

        override fun isCommandAvailable(command: Int): Boolean =
            getAvailableCommands().contains(command)

        override fun seekToNext() = if (freeMode) advanceManual(+1) else super.seekToNext()
        override fun seekToNextMediaItem() = if (freeMode) advanceManual(+1) else super.seekToNextMediaItem()
        override fun seekToPrevious() = if (freeMode) advanceManual(-1) else super.seekToPrevious()
        override fun seekToPreviousMediaItem() = if (freeMode) advanceManual(-1) else super.seekToPreviousMediaItem()
    }

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (!freeMode) return
            // Reklam→şarkı geçişi: artık reklam değil bir öğeye geçildiyse reklamı tamamlanmış say.
            val impression = pendingImpressionId ?: return
            val id = mediaItem?.mediaId.orEmpty()
            if (!id.startsWith(PlaybackCommands.AD_MEDIA_ID_PREFIX)) {
                pendingImpressionId = null
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { playbackRepository.completeAd(impression) } }
                }
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (!freeMode) return
            // Mantıksal şarkı bitti → sıradakini çöz (arka planda da çalışır).
            if (playbackState == Player.STATE_ENDED) advanceAuto()
        }
    }

    init {
        exoPlayer.addListener(listener)
    }

    /** Free kuyruğu başlatır (custom komuttan). Verilen index'teki şarkıyı çözüp çalar. */
    fun startFree(newTracks: List<FreeTrack>, startIndex: Int) {
        if (newTracks.isEmpty()) return
        freeMode = true
        tracks = newTracks
        index = startIndex.coerceIn(0, newTracks.lastIndex)
        playCurrent()
    }

    /**
     * Custom komuttan ("sonraki") gelen ilerleme (in-app/mini buton). Reklam sırasında yok sayılır
     * ([advanceManual] içindeki kural), şarkı sırasında bir sonraki parçaya geçer.
     */
    fun next() = advanceManual(+1)

    /** Kontrolcü/bildirim kaynaklı önceki/sonraki; reklam atlanamaz. */
    private fun advanceManual(delta: Int) {
        if (currentIsAd) return
        if (delta > 0 && hasNext) {
            index++; playCurrent()
        } else if (delta < 0) {
            if (hasPrevious) {
                index--; playCurrent()
            } else {
                exoPlayer.seekTo(0L) // ilk şarkıda geri → başa sar
            }
        }
    }

    /** Şarkı bitince otomatik sonraki; son şarkıda durur. */
    private fun advanceAuto() {
        if (hasNext) {
            index++; playCurrent()
        }
    }

    private fun playCurrent() {
        val track = tracks.getOrNull(index) ?: return
        resolveJob?.cancel()
        resolveJob = scope.launch {
            try {
                val resolution = withContext(Dispatchers.IO) { playbackRepository.resolveNext(track.id) }
                pendingImpressionId = (resolution as? PlaybackResolution.WithAd)?.impressionId
                exoPlayer.setMediaItems(resolution.toMediaItems(), 0, 0L)
                exoPlayer.prepare()
                exoPlayer.play()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // En iyi çaba: çözülemezse oynatma durur; uygulama çökmez.
                pendingImpressionId = null
            }
        }
    }

    /** Listener'ı kaldırır ve kapsamı iptal eder (oynatıcının kendisini SERBEST BIRAKMAZ). */
    fun release() {
        resolveJob?.cancel()
        scope.cancel()
        exoPlayer.removeListener(listener)
    }

    private fun PlaybackResolution.toMediaItems(): List<MediaItem> = when (this) {
        is PlaybackResolution.SongOnly -> listOf(songItem(song, streamUrl))
        is PlaybackResolution.WithAd ->
            listOf(adItem(ad, adStreamUrl, impressionId), songItem(song, streamUrl))
    }

    private fun songItem(song: Song, stream: StreamUrl): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .build()
        return MediaItem.Builder()
            .setMediaId(song.id)
            // Gerçek (çözülmüş) URL: free'de resolver/stream-url devreye girmez.
            .setUri(stream.url)
            .setCustomCacheKey(song.id)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun adItem(ad: AdInfo, stream: StreamUrl, impressionId: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(ad.title)
            .setArtist(ad.advertiser)
            .build()
        return MediaItem.Builder()
            .setMediaId(PlaybackCommands.AD_MEDIA_ID_PREFIX + impressionId)
            .setUri(stream.url)
            .setMediaMetadata(metadata)
            .build()
    }
}
