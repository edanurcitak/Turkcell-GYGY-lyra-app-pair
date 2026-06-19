package com.turkcell.lyraapp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * LyraApp ikon seti.
 *
 * Material Icons bağımlılığı eklemeden, ekranların ihtiyaç duyduğu glyph'leri
 * 24x24 viewport'lu [ImageVector] olarak tanımlar. Path'in dolgu rengi önemsizdir;
 * `Icon(...)` composable'ı `tint` ile üzerine yazar. Bu yüzden tüm path'ler
 * [Color.Black] ile doldurulur ve renk daima çağrı tarafında temadan okunur.
 */
object LyraIcons {

    /** Marka logosu: ekolayzer/dalga formu çubukları (Material GraphicEq). */
    val Waveform: ImageVector by lazy {
        lyraIcon(
            name = "Waveform",
            pathData = "M7,18h2V6H7v12zM11,22h2V2h-2v20zM3,14h2v-4H3v4zM15,18h2V6h-2v12zM19,10v4h2v-4h-2z",
        )
    }

    /** Telefon numarası alanının leading ikonu (Material Smartphone, outlined). */
    val Smartphone: ImageVector by lazy {
        lyraIcon(
            name = "Smartphone",
            pathData = "M15.5,1h-8C6.12,1 5,2.12 5,3.5v17C5,21.88 6.12,23 7.5,23h8c1.38,0 " +
                    "2.5,-1.12 2.5,-2.5v-17C18,2.12 16.88,1 15.5,1zM13,21h-3v-1h3v1zM16.25,18H6.75V4h9.5V18z",
        )
    }

    /** Şifre alanının leading ikonu (Material Lock). */
    val Lock: ImageVector by lazy {
        lyraIcon(
            name = "Lock",
            pathData = "M18,8h-1V6c0,-2.76 -2.24,-5 -5,-5S7,3.24 7,6v2H6c-1.1,0 -2,0.9 -2,2v10c0," +
                    "1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V10c0,-1.1 -0.9,-2 -2,-2zM12,17c-1.1,0 -2,-0.9 " +
                    "-2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2zM15.1,8H8.9V6c0,-1.71 1.39,-3.1 3.1,-3.1 " +
                    "1.71,0 3.1,1.39 3.1,3.1v2z",
        )
    }

    /** Şifre görünürlük (göz) ikonu (Material Visibility). */
    val Visibility: ImageVector by lazy {
        lyraIcon(
            name = "Visibility",
            pathData = "M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73," +
                    "-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 " +
                    "-5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z",
        )
    }

    /** Giriş butonu ileri oku (Material ArrowForward). */
    val ArrowForward: ImageVector by lazy {
        lyraIcon(
            name = "ArrowForward",
            pathData = "M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z",
        )
    }

    /** Üst bar geri oku (Material ArrowBack). */
    val ArrowBack: ImageVector by lazy {
        lyraIcon(
            name = "ArrowBack",
            pathData = "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z",
        )
    }

    /** BNB: Ana sayfa sekmesi (Material Home). */
    val Home: ImageVector by lazy {
        lyraIcon(
            name = "Home",
            pathData = "M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z",
        )
    }

    /** BNB: Ara sekmesi (Material Search). */
    val Search: ImageVector by lazy {
        lyraIcon(
            name = "Search",
            pathData = "M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 " +
                    "9.5,3S3,5.91 3,9.5 5.91,16 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5," +
                    "4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5S7.01,5 9.5,5 14,7.01 14," +
                    "9.5 11.99,14 9.5,14z",
        )
    }

    /** BNB: Kütüphane sekmesi (Material LibraryMusic). */
    val Library: ImageVector by lazy {
        lyraIcon(
            name = "Library",
            pathData = "M20,2H8C6.9,2 6,2.9 6,4v12c0,1.1 0.9,2 2,2h12c1.1,0 2,-0.9 2,-2V4c0,-1.1 " +
                    "-0.9,-2 -2,-2zM18,7h-3v5.5c0,1.38 -1.12,2.5 -2.5,2.5S10,13.88 10,12.5s1.12,-2.5 " +
                    "2.5,-2.5c0.57,0 1.08,0.19 1.5,0.51V5h4v2zM4,6L2,6v14c0,1.1 0.9,2 2,2h14v-2L4,20L4,6z",
        )
    }

    /** BNB: Favoriler sekmesi (Material Favorite). */
    val Favorite: ImageVector by lazy {
        lyraIcon(
            name = "Favorite",
            pathData = "M12,21.35l-1.45,-1.32C5.4,15.36 2,12.28 2,8.5 2,5.42 4.42,3 7.5,3c1.74,0 " +
                    "3.41,0.81 4.5,2.09C13.09,3.81 14.76,3 16.5,3 19.58,3 22,5.42 22,8.5c0,3.78 " +
                    "-3.4,6.86 -8.55,11.54L12,21.35z",
        )
    }

    /** BNB: Profil sekmesi (Material Person). */
    val Profile: ImageVector by lazy {
        lyraIcon(
            name = "Profile",
            pathData = "M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67," +
                    "0 -8,1.34 -8,4v2h16v-2c0,-2.66 -5.33,-4 -8,-4z",
        )
    }

    /** Kütüphane üst barı: çalma listesi ekle (Material Add). */
    val Add: ImageVector by lazy {
        lyraIcon(
            name = "Add",
            pathData = "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z",
        )
    }

    /** Seçili filtre çipinin leading onayı (Material Check). */
    val Check: ImageVector by lazy {
        lyraIcon(
            name = "Check",
            pathData = "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z",
        )
    }

    /** Çalma listesi satırı: "daha fazla" menüsü (Material MoreVert). */
    val MoreVert: ImageVector by lazy {
        lyraIcon(
            name = "MoreVert",
            pathData = "M12,8c1.1,0 2,-0.9 2,-2s-0.9,-2 -2,-2 -2,0.9 -2,2 0.9,2 2,2zM12,10c-1.1,0 " +
                    "-2,0.9 -2,2s0.9,2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2zM12,16c-1.1,0 -2,0.9 -2,2s0.9," +
                    "2 2,2 2,-0.9 2,-2 -0.9,-2 -2,-2z",
        )
    }

    /** Sabitlenmiş çalma listesi göstergesi (Material PushPin). */
    val PushPin: ImageVector by lazy {
        lyraIcon(
            name = "PushPin",
            pathData = "M16,9V4l1,0c0.55,0 1,-0.45 1,-1v0c0,-0.55 -0.45,-1 -1,-1H7C6.45,2 6,2.45 6,3v0c0," +
                    "0.55 0.45,1 1,1l1,0v5c0,1.66 -1.34,3 -3,3v2h5.97v7l1,1 1,-1v-7H19v-2C17.34,12 " +
                    "16,10.66 16,9z",
        )
    }

    /** Sıralama satırı: sıralama düzenini değiştir (Material SwapVert). */
    val SwapVert: ImageVector by lazy {
        lyraIcon(
            name = "SwapVert",
            pathData = "M16,17.01V10h-2v7.01h-3L15,21l4,-3.99h-3zM9,3L5,6.99h3V14h2V6.99h3L9,3z",
        )
    }

    /** Görünüm değiştirici: ızgara görünümü (Material GridView). */
    val GridView: ImageVector by lazy {
        lyraIcon(
            name = "GridView",
            pathData = "M3,3v8h8V3H3zM9,9H5V5h4V9zM3,13v8h8v-8H3zM9,19H5v-4h4V19zM13,3v8h8V3h-8zM19," +
                    "9h-4V5h4V9zM13,13v8h8v-8h-8zM19,19h-4v-4h4V19z",
        )
    }

    /** Görünüm değiştirici: liste görünümü (Material ViewList). */
    val ViewList: ImageVector by lazy {
        lyraIcon(
            name = "ViewList",
            pathData = "M4,14h4v-4L4,10v4zM4,19h4v-4L4,15v4zM4,9h4L8,5L4,5v4zM9,14h12v-4L9,10v4zM9," +
                    "19h12v-4L9,15v4zM9,5v4h12L21,5L9,5z",
        )
    }

    // ── Player ekranı ikonları ──

    /** Player üst barı: ekranı küçült/kapat (Material ExpandMore). */
    val ExpandMore: ImageVector by lazy {
        lyraIcon(
            name = "ExpandMore",
            pathData = "M16.59,8.59L12,13.17 7.41,8.59 6,10l6,6 6,-6z",
        )
    }

    /** Player kontrolü: karıştır (Material Shuffle). */
    val Shuffle: ImageVector by lazy {
        lyraIcon(
            name = "Shuffle",
            pathData = "M10.59,9.17L5.41,4 4,5.41l5.17,5.17 1.42,-1.41zM14.5,4l2.04,2.04L4,18.59 " +
                    "5.41,20 17.96,7.46 20,9.5L20,4zM14.83,13.41l-1.41,1.41 3.13,3.13L14.5,20L20," +
                    "20v-5.5l-2.04,2.04z",
        )
    }

    /** Player kontrolü: önceki şarkı (Material SkipPrevious). */
    val SkipPrevious: ImageVector by lazy {
        lyraIcon(
            name = "SkipPrevious",
            pathData = "M6,6h2v12L6,18zM9.5,12l8.5,6L18,6z",
        )
    }

    /** Player kontrolü: sonraki şarkı (Material SkipNext). */
    val SkipNext: ImageVector by lazy {
        lyraIcon(
            name = "SkipNext",
            pathData = "M6,18l8.5,-6L6,6v12zM16,6v12h2L18,6z",
        )
    }

    /** Player kontrolü: oynat (Material PlayArrow). */
    val PlayArrow: ImageVector by lazy {
        lyraIcon(
            name = "PlayArrow",
            pathData = "M8,5v14l11,-7z",
        )
    }

    /** Player kontrolü: duraklat (Material Pause). */
    val Pause: ImageVector by lazy {
        lyraIcon(
            name = "Pause",
            pathData = "M6,19h4L10,5L6,5v14zM14,5v14h4L18,5h-4z",
        )
    }

    /** Player kontrolü: tekrarla (Material Repeat). */
    val Repeat: ImageVector by lazy {
        lyraIcon(
            name = "Repeat",
            pathData = "M7,7h10v3l4,-4 -4,-4v3L5,5v6h2L7,7zM17,17L7,17v-3l-4,4 4,4v-3h12v-6h-2v4z",
        )
    }

    /** Player alt barı: cihazlara yayınla (Material Cast). */
    val Cast: ImageVector by lazy {
        lyraIcon(
            name = "Cast",
            pathData = "M21,3L3,3c-1.1,0 -2,0.9 -2,2v3h2L3,5h18v14h-7v2h7c1.1,0 2,-0.9 2,-2L23," +
                    "5c0,-1.1 -0.9,-2 -2,-2zM1,18v3h3c0,-1.66 -1.34,-3 -3,-3zM1,14v2c2.76,0 5," +
                    "2.24 5,5h2c0,-3.87 -3.13,-7 -7,-7zM1,10v2c4.97,0 9,4.03 9,9h2c0,-6.08 " +
                    "-4.93,-11 -11,-11z",
        )
    }

    /** Player alt barı: bildirim/arka plan (Material Notifications). */
    val Notifications: ImageVector by lazy {
        lyraIcon(
            name = "Notifications",
            pathData = "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zM18,16v-5c0,-3.07 -1.63,-5.64 " +
                    "-4.5,-6.32L13.5,4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.64," +
                    "5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z",
        )
    }

    /** Player alt barı: çalma sırası/kuyruk (Material QueueMusic). */
    val QueueMusic: ImageVector by lazy {
        lyraIcon(
            name = "QueueMusic",
            pathData = "M15,6H3v2h12V6zM15,10H3v2h12v-2zM3,16h8v-2H3v2zM17,6v8.18c-0.31,-0.11 " +
                    "-0.65,-0.18 -1,-0.18 -1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3V8h3V6h-5z",
        )
    }
}

/**
 * 24x24 viewport'lu, tek path'li bir [ImageVector] üretir.
 * Path verisi standart SVG/Android `pathData` string formatındadır.
 */
private fun lyraIcon(name: String, pathData: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black),
    ).build()