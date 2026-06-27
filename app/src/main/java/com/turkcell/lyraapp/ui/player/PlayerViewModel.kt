package com.turkcell.lyraapp.ui.player

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.download.DownloadStore
import com.turkcell.lyraapp.data.download.PremiumRequiredException
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.data.membership.MembershipStore
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.ui.navigation.LyraDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import android.graphics.Color as AndroidColor

/**
 * Player ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Oynatma, [PlaybackService]'in sahip olduğu ExoPlayer üzerinde yürür; bu ViewModel servise bir
 * [MediaController] ile bağlanır. Böylece şarkı, ekrandan çıkıldığında veya uygulama arka plana
 * atıldığında da çalmaya devam eder ve Media3 görseldeki sistem medya bildirimini üretir.
 *
 * **Kuyruk:** `queue` nav argümanına göre bağlama duyarlı bir kuyruk kurulur — Feed'den açıldıysa
 * şarkı kataloğu ([SongRepository.getSongs]), çalma listesinden açıldıysa o listenin şarkıları
 * ([PlaylistRepository.getPlaylistDetail]). Bu sayede bildirimdeki ve tam ekrandaki önceki/sonraki
 * butonları gerçek parçalar arasında gezer. Aktif parça değişince (bildirimden de olabilir) UI
 * [onMediaItemTransition] ile senkron tutulur.
 *
 * Stream URL'leri kısa ömürlü olduğundan kuyruk parçaları `lyra://song/<id>` yer tutucusuyla
 * kurulur; gerçek URL servis tarafında çalma anında çözülür (bkz. [PlaybackService]).
 *
 * songId/title/artist/queue navigasyon argümanı olarak [SavedStateHandle]'dan okunur —
 * `NavController` ViewModel'e sızmaz (mevcut konvansiyon).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val downloadRepository: DownloadRepository,
    private val downloadStore: DownloadStore,
    private val membershipStore: MembershipStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val songId: String = checkNotNull(savedStateHandle[LyraDestinations.PLAYER_ARG_SONG_ID]) {
        "PlayerViewModel için 'songId' nav argümanı zorunludur."
    }
    private val title: String = savedStateHandle[LyraDestinations.PLAYER_ARG_TITLE] ?: ""
    private val artist: String = savedStateHandle[LyraDestinations.PLAYER_ARG_ARTIST] ?: ""
    private val queue: String =
        savedStateHandle[LyraDestinations.PLAYER_ARG_QUEUE] ?: LyraDestinations.QUEUE_FEED

    private val _uiState = MutableStateFlow(
        PlayerUiState(songId = songId, title = title, artist = artist),
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    // İndir intent'inde tam Song'u (albüm/süre dahil) bulabilmek için en son yüklenen kuyruk.
    private var currentSongs: List<Song> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrentItem(mediaItem)
        }
    }

    init {
        observeDownloads()
        observePremium()
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        // Bağlantı tamamlanınca (ana iş parçacığında) controller hazır olur.
        future.addListener(
            {
                val c = future.get()
                controller = c
                c.addListener(playerListener)
                _uiState.update { it.copy(isPlaying = c.isPlaying) }
                observePosition()
                load()
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    fun onIntent(intent: PlayerIntent) {
        when (intent) {
            PlayerIntent.PlayPause -> controller?.let { if (it.isPlaying) it.pause() else it.play() }
            is PlayerIntent.SeekTo -> controller?.seekTo(intent.positionMs)
            PlayerIntent.SkipNext -> controller?.seekToNext()
            PlayerIntent.SkipPrevious -> controller?.seekToPrevious()
            PlayerIntent.Download -> onDownloadClicked()
            PlayerIntent.Retry -> load()
        }
    }

    /** İndir aksiyonu: premium ise indir, free ise "Premium gerekli" ipucunu göster. */
    private fun onDownloadClicked() {
        if (membershipStore.isPremium) {
            downloadCurrentSong()
        } else {
            _uiState.update { it.copy(showPremiumHint = true) }
        }
    }

    /** [DownloadStore] değiştikçe aktif şarkının indirilme durumunu UI'a yansıtır. */
    private fun observeDownloads() {
        viewModelScope.launch {
            downloadStore.downloads.collect { downloads ->
                _uiState.update { state ->
                    state.copy(isDownloaded = downloads.any { it.id == state.songId })
                }
            }
        }
    }

    /** Tier'ı (free/premium) UI'a yansıtır; kaynak API (bkz. [MembershipStore]). */
    private fun observePremium() {
        viewModelScope.launch {
            membershipStore.isPremiumFlow.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
    }

    /**
     * Aktif şarkıyı cihaza indirir. Tam meta veriyi (albüm/süre) yüklü kuyruktan alır; bulunamazsa
     * UI'daki bilgiyle (süreyi controller'dan) bir [Song] kurar. İndirme hatası oynatmayı etkilemez.
     */
    private fun downloadCurrentSong() {
        val state = _uiState.value
        if (state.isDownloaded || state.isDownloading || state.songId.isEmpty()) return
        val song = currentSongs.firstOrNull { it.id == state.songId }
            ?: Song(
                id = state.songId,
                title = state.title,
                artist = state.artist,
                album = null,
                durationMs = controller?.duration?.takeIf { it > 0L } ?: 0L,
            )
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true) }
            try {
                downloadRepository.download(song)
                _uiState.update { it.copy(isDownloading = false, isDownloaded = true) }
            } catch (e: PremiumRequiredException) {
                // Savunma: tier'ın nihai otoritesi sunucu — 403 ise free'ye düş ve upsell göster.
                _uiState.update {
                    it.copy(isDownloading = false, isPremium = false, showPremiumHint = true)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // En iyi çaba: indirme başarısızlığı yalnızca durumu sıfırlar, oynatmayı bozmaz.
                _uiState.update { it.copy(isDownloading = false) }
            }
        }
    }

    /** Aktif parçanın meta verisini (id/başlık/sanatçı) UI'a yansıtır (kapak songId'den türer). */
    private fun syncCurrentItem(mediaItem: MediaItem?) {
        val item = mediaItem ?: return
        _uiState.update {
            val newSongId = item.mediaId.ifEmpty { it.songId }
            it.copy(
                songId = newSongId,
                title = item.mediaMetadata.title?.toString() ?: it.title,
                artist = item.mediaMetadata.artist?.toString() ?: it.artist,
                isDownloaded = downloadStore.isDownloaded(newSongId),
            )
        }
    }

    /** Controller'dan pozisyon/süreyi periyodik yansıtır (çalma durumu listener ile gelir). */
    private fun observePosition() {
        viewModelScope.launch {
            while (isActive) {
                controller?.let { c ->
                    val duration = c.duration.let { if (it > 0) it else 0L }
                    _uiState.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            durationMs = duration,
                        )
                    }
                }
                delay(POSITION_POLL_INTERVAL_MS)
            }
        }
    }

    private fun load() {
        val c = controller ?: return
        // Aynı şarkı zaten (arka planda) çalıyorsa kuyruğu yeniden kurma; mevcut duruma bağlan.
        if (c.currentMediaItem?.mediaId == songId && c.mediaItemCount > 0) {
            _uiState.update { it.copy(isLoading = false, errorMessage = null) }
            syncCurrentItem(c.currentMediaItem)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val loaded = loadQueueSongs()
                // Tıklanan şarkı listede yoksa (uç durum) bağlamı yok say, tek parçalık kuyruk kur.
                val songs = if (loaded.any { it.id == songId }) {
                    loaded
                } else {
                    listOf(Song(id = songId, title = title, artist = artist, album = null, durationMs = 0L))
                }
                currentSongs = songs
                val startIndex = songs.indexOfFirst { it.id == songId }.coerceAtLeast(0)

                // Başlangıç parçasının kapağını senkron üret (hızlı başlasın); gerisi arka planda.
                val startArtwork = withContext(Dispatchers.Default) { artworkPng(songs[startIndex].id) }
                val items = songs.mapIndexed { index, song ->
                    buildQueueItem(song, artwork = if (index == startIndex) startArtwork else null)
                }

                c.setMediaItems(items, startIndex, 0L)
                c.prepare()
                c.play()
                _uiState.update { it.copy(isLoading = false) }
                syncCurrentItem(c.currentMediaItem)
                fillRemainingArtwork(songs, startIndex)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Şarkı yüklenemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }

    /** Kuyruk kaynağına göre şarkı listesini getirir (Feed kataloğu veya çalma listesi). */
    private suspend fun loadQueueSongs(): List<Song> =
        if (queue.startsWith(LyraDestinations.QUEUE_PLAYLIST_PREFIX)) {
            val playlistId = queue.removePrefix(LyraDestinations.QUEUE_PLAYLIST_PREFIX)
            playlistRepository.getPlaylistDetail(playlistId).songs
        } else {
            songRepository.getSongs()
        }

    private fun buildQueueItem(song: Song, artwork: ByteArray?): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .apply {
                if (artwork != null) setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            }
            .build()
        return MediaItem.Builder()
            .setMediaId(song.id)
            // Gerçek stream URL'i servis tarafında çözülür (kısa ömürlü/imzalı URL).
            .setUri("$SONG_URI_PREFIX${Uri.encode(song.id)}")
            // Cache key = songId: indirilmiş şarkı, imzalı URL değişse de cache'te bu anahtarla
            // bulunur ve çevrimdışı çalar (bkz. MediaDownloadRepository / PlaybackService).
            .setCustomCacheKey(song.id)
            .setMediaMetadata(metadata)
            .build()
    }

    /**
     * Başlangıç dışındaki parçaların kapaklarını arka planda üretip yerine koyar; böylece sonraki
     * şarkıya geçilince bildirim kapağı hazır olur. Aktif parçayı değiştirmek oynatmayı keseceği
     * için o atlanır (kapağı zaten ilk kuruluşta vardır).
     */
    private fun fillRemainingArtwork(songs: List<Song>, startIndex: Int) {
        viewModelScope.launch {
            songs.forEachIndexed { index, song ->
                if (index == startIndex) return@forEachIndexed
                val artwork = withContext(Dispatchers.Default) { artworkPng(song.id) }
                val c = controller ?: return@launch
                if (index == c.currentMediaItemIndex) return@forEachIndexed
                if (index !in 0 until c.mediaItemCount) return@forEachIndexed
                val existing = c.getMediaItemAt(index)
                if (existing.mediaId != song.id) return@forEachIndexed
                val updated = existing.buildUpon()
                    .setMediaMetadata(
                        existing.mediaMetadata.buildUpon()
                            .setArtworkData(artwork, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                            .build(),
                    )
                    .build()
                c.replaceMediaItem(index, updated)
            }
        }
    }

    override fun onCleared() {
        // Controller'ı bırak; ExoPlayer'ı DEĞİL — servis arka planda çalmaya devam etsin.
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }

    private companion object {
        const val POSITION_POLL_INTERVAL_MS = 500L
        const val SONG_URI_PREFIX = "lyra://song/"
    }
}

/**
 * [songId]'den, uygulama içi kapakla aynı görünümde (deterministik renk + eşmerkezli halkalar)
 * bir kapak üretir ve bildirimde kullanılmak üzere PNG bayt dizisi olarak döndürür.
 *
 * Not (§2.2): API'da şarkı kapağı yok; renk, Feed/PlaylistDetail/Player ile aynı `hashCode`
 * türetiminden gelir — yeni veri uydurulmaz, mevcut görselle tutarlı kalır.
 */
private fun artworkPng(songId: String, sizePx: Int = 512): ByteArray {
    val base = artworkColorArgb(songId)
    val light = blendColor(base, AndroidColor.WHITE, 0.22f)
    val dark = blendColor(base, AndroidColor.BLACK, 0.30f)

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val size = sizePx.toFloat()

    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f, 0f, size, size,
            intArrayOf(light, base, dark), null, Shader.TileMode.CLAMP,
        )
    }
    canvas.drawRect(0f, 0f, size, size, fill)

    val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = AndroidColor.argb((0.07f * 255).toInt(), 255, 255, 255)
        strokeWidth = size * 0.018f
    }
    val centerX = size * 0.66f
    val centerY = size * 0.46f
    val maxRadius = size * 0.95f
    val rings = 6
    for (i in 1..rings) {
        canvas.drawCircle(centerX, centerY, maxRadius * i / rings, ring)
    }

    return ByteArrayOutputStream().use { stream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        bitmap.recycle()
        stream.toByteArray()
    }
}

/** Feed/PlaylistDetail/Player ile aynı deterministik kapak rengi (ARGB int). */
private fun artworkColorArgb(id: String): Int {
    val hue = (((id.hashCode() % 360) + 360) % 360).toFloat()
    return AndroidColor.HSVToColor(floatArrayOf(hue, 0.5f, 0.6f))
}

/** İki ARGB rengi doğrusal harmanlar (t=0 → from, t=1 → to). */
private fun blendColor(from: Int, to: Int, t: Float): Int {
    val r = (AndroidColor.red(from) + (AndroidColor.red(to) - AndroidColor.red(from)) * t).toInt()
    val g = (AndroidColor.green(from) + (AndroidColor.green(to) - AndroidColor.green(from)) * t).toInt()
    val b = (AndroidColor.blue(from) + (AndroidColor.blue(to) - AndroidColor.blue(from)) * t).toInt()
    return AndroidColor.rgb(r, g, b)
}
