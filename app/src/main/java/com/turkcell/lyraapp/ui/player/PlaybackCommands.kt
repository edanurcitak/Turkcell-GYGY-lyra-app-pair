package com.turkcell.lyraapp.ui.player

import android.os.Bundle

/**
 * Free oynatma için [PlayerViewModel] (MediaController) ↔ [PlaybackService] (MediaSession) arasındaki
 * custom komut sözleşmesi.
 *
 * Free akışta kuyruk servis tarafında, parça-parça `playback/next` ile çözülür; bu yüzden çalınacak
 * şarkı listesi (id + başlık/sanatçı) ve başlangıç index'i servise bu komutla geçirilir. Premium
 * akış bu komutu kullanmaz (mevcut `setMediaItems` yolundan gider).
 */
object PlaybackCommands {

    /** Free kuyruğu başlatma custom komutu (Bundle yükü [playFreeArgs] ile kurulur). */
    const val COMMAND_PLAY_FREE = "com.turkcell.lyraapp.PLAY_FREE"

    /**
     * Free akışta "sonraki şarkı" custom komutu.
     *
     * Free kuyruğu mantıksal olarak tek parça olduğundan `COMMAND_SEEK_TO_NEXT` ExoPlayer'da yerel
     * mevcut değil; `MediaController.seekToNext()` kontrolcüde sessizce düşürülür. Custom komut yalnızca
     * session izniyle gated olduğundan güvenilir biçimde servise ulaşır (önceki yön zaten yerel çalışıyor).
     */
    const val COMMAND_FREE_NEXT = "com.turkcell.lyraapp.FREE_NEXT"

    /** Reklam parçalarının mediaId öneki (ad-complete tetikleme + UI reklam göstergesi için). */
    const val AD_MEDIA_ID_PREFIX = "ad:"

    private const val KEY_IDS = "ids"
    private const val KEY_TITLES = "titles"
    private const val KEY_ARTISTS = "artists"
    private const val KEY_START_INDEX = "startIndex"

    /** Free kuyruğu komutunun Bundle yükünü kurar. */
    fun playFreeArgs(tracks: List<FreeTrack>, startIndex: Int): Bundle = Bundle().apply {
        putStringArray(KEY_IDS, tracks.map { it.id }.toTypedArray())
        putStringArray(KEY_TITLES, tracks.map { it.title }.toTypedArray())
        putStringArray(KEY_ARTISTS, tracks.map { it.artist }.toTypedArray())
        putInt(KEY_START_INDEX, startIndex)
    }

    /** Bundle yükünü (parça listesi + başlangıç index) çözer. */
    fun parsePlayFree(args: Bundle): PlayFreeArgs {
        val ids = args.getStringArray(KEY_IDS) ?: emptyArray()
        val titles = args.getStringArray(KEY_TITLES) ?: emptyArray()
        val artists = args.getStringArray(KEY_ARTISTS) ?: emptyArray()
        val tracks = ids.mapIndexed { i, id ->
            FreeTrack(
                id = id,
                title = titles.getOrElse(i) { "" },
                artist = artists.getOrElse(i) { "" },
            )
        }
        return PlayFreeArgs(tracks = tracks, startIndex = args.getInt(KEY_START_INDEX, 0))
    }
}

/** Free kuyruğundaki bir parçanın taşınan asgari meta verisi (URL servis tarafında çözülür). */
data class FreeTrack(
    val id: String,
    val title: String,
    val artist: String,
)

/** [PlaybackCommands.parsePlayFree] sonucu. */
data class PlayFreeArgs(
    val tracks: List<FreeTrack>,
    val startIndex: Int,
)
