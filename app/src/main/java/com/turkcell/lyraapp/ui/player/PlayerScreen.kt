package com.turkcell.lyraapp.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Player ("Şimdi Çalıyor") ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [PlayerViewModel]'den toplar, görsel içeriği stateless gövdeye devreder. Oynat/duraklat
 * ve ilerleme çubuğu intent üzerinden ExoPlayer'a bağlıdır; favori ve karıştır/önceki/sonraki/
 * tekrarla butonları şimdilik görseldir (kuyruk/favori verisi taşınmadığından no-op).
 */
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlayerScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/** Player ekranının stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri dışarı bildirir. */
@Composable
private fun PlayerScreen(
    uiState: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surface
    val baseColor = remember(uiState.songId) { artworkColorFor(uiState.songId) }
    // Üstte şarkı renginin koyu tonu → altta yüzey: görseldeki sıcak gradyan.
    val background = remember(baseColor, surface) {
        Brush.verticalGradient(
            listOf(lerp(baseColor, surface, 0.35f), surface, surface),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        TopBar(onCollapse = onNavigateBack)

        if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                ErrorState(
                    message = uiState.errorMessage,
                    onRetry = { onIntent(PlayerIntent.Retry) },
                )
            }
        } else {
            Spacer(Modifier.height(24.dp))
            Artwork(
                songId = uiState.songId,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
            Spacer(Modifier.height(28.dp))
            NowPlayingInfo(
                title = uiState.title,
                artist = uiState.artist,
                isFavorite = uiState.isFavorite,
                isDownloaded = uiState.isDownloaded,
                isDownloading = uiState.isDownloading,
                isPremium = uiState.isPremium,
                onToggleFavorite = { /* §talep: favori şimdilik boş */ },
                onDownload = { onIntent(PlayerIntent.Download) },
            )
            if (uiState.showPremiumHint) {
                Spacer(Modifier.height(8.dp))
                PremiumHint()
            }
            Spacer(Modifier.height(20.dp))
            ProgressSection(uiState = uiState, onIntent = onIntent)
            Spacer(Modifier.height(24.dp))
            Controls(
                isPlaying = uiState.isPlaying,
                isLoading = uiState.isLoading,
                onPlayPause = { onIntent(PlayerIntent.PlayPause) },
                onSkipPrevious = { onIntent(PlayerIntent.SkipPrevious) },
                onSkipNext = { onIntent(PlayerIntent.SkipNext) },
            )
            Spacer(Modifier.weight(1f))
        }

        BottomBar()
    }
}

@Composable
private fun TopBar(onCollapse: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = LyraIcons.ExpandMore,
                contentDescription = "Küçült",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "ŞİMDİ ÇALIYOR",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )
        IconButton(onClick = { /* taşma menüsü: kapsam dışı, görsel */ }) {
            Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * Kapak görseli. API'da şarkı için renk olmadığından (§2.2) renk [songId]'den deterministik
 * türetilir (Feed/PlaylistDetail ile aynı `artworkColorFor`); üzerine eşmerkezli halkalar çizilir.
 */
@Composable
private fun Artwork(
    songId: String,
    modifier: Modifier = Modifier,
) {
    val base = remember(songId) { artworkColorFor(songId) }
    val light = remember(base) { lerp(base, Color.White, 0.22f) }
    val dark = remember(base) { lerp(base, Color.Black, 0.30f) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(light, base, dark))),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.66f, size.height * 0.46f)
            val maxRadius = size.minDimension * 0.95f
            val rings = 6
            for (i in 1..rings) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = maxRadius * i / rings,
                    center = center,
                    style = Stroke(width = size.minDimension * 0.018f),
                )
            }
        }
    }
}

@Composable
private fun NowPlayingInfo(
    title: String,
    artist: String,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    isPremium: Boolean,
    onToggleFavorite: () -> Unit,
    onDownload: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DownloadButton(
            isPremium = isPremium,
            isDownloaded = isDownloaded,
            isDownloading = isDownloading,
            onDownload = onDownload,
        )
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = if (isFavorite) "Favorilerden çıkar" else "Favorilere ekle",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/**
 * Kalbin yanındaki "İndir" butonu. Durumlar: indiriliyor (ilerleme halkası), indirildi (onay),
 * free (kilit → tıklayınca "Premium gerekli" ipucu) ve premium-indirilebilir (indirme ikonu).
 *
 * Çevrimdışı indirme premium'a özel olduğundan (tier kaynağı API), free hesapta buton kilit
 * gösterir ama tıklanabilir kalır; tıklama [onDownload] → ViewModel ipucunu açar.
 */
@Composable
private fun DownloadButton(
    isPremium: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    onDownload: () -> Unit,
) {
    IconButton(
        onClick = onDownload,
        enabled = !isDownloaded && !isDownloading,
    ) {
        when {
            isDownloading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
            )

            isDownloaded -> Icon(
                imageVector = LyraIcons.Check,
                contentDescription = "İndirildi",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )

            !isPremium -> Icon(
                imageVector = LyraIcons.Lock,
                contentDescription = "Premium gerekli",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )

            else -> Icon(
                imageVector = LyraIcons.Download,
                contentDescription = "İndir",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

/** Free hesap indirmeyi denediğinde gösterilen kısa upsell satırı. */
@Composable
private fun PremiumHint() {
    Text(
        text = "Çevrimdışı indirme Premium üyelere özeldir.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ProgressSection(
    uiState: PlayerUiState,
    onIntent: (PlayerIntent) -> Unit,
) {
    var dragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val fraction = if (uiState.durationMs > 0L) {
        (uiState.positionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val sliderValue = if (dragging) dragFraction else fraction
    val shownPositionMs = if (dragging) (dragFraction * uiState.durationMs).toLong() else uiState.positionMs

    Column {
        Slider(
            value = sliderValue,
            onValueChange = {
                dragging = true
                dragFraction = it
            },
            onValueChangeFinished = {
                dragging = false
                onIntent(PlayerIntent.SeekTo((dragFraction * uiState.durationMs).toLong()))
            },
            enabled = uiState.durationMs > 0L,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
            ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = formatTime(shownPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = formatTime(uiState.durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Controls(
    isPlaying: Boolean,
    isLoading: Boolean,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlIcon(
            icon = LyraIcons.Shuffle,
            contentDescription = "Karıştır",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            iconSize = 24.dp,
        )
        ControlIcon(
            icon = LyraIcons.SkipPrevious,
            contentDescription = "Önceki",
            tint = MaterialTheme.colorScheme.onSurface,
            iconSize = 36.dp,
            onClick = onSkipPrevious,
        )
        PlayPauseButton(isPlaying = isPlaying, isLoading = isLoading, onClick = onPlayPause)
        ControlIcon(
            icon = LyraIcons.SkipNext,
            contentDescription = "Sonraki",
            tint = MaterialTheme.colorScheme.onSurface,
            iconSize = 36.dp,
            onClick = onSkipNext,
        )
        ControlIcon(
            icon = LyraIcons.Repeat,
            contentDescription = "Tekrarla",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            iconSize = 24.dp,
        )
    }
}

/**
 * Kontrol ikonu. Önceki/sonraki için [onClick] kuyruğu sürer; karıştır/tekrarla için
 * kuyruk/ayar verisi taşınmadığından varsayılan no-op kalır.
 */
@Composable
private fun ControlIcon(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    iconSize: Dp,
    onClick: () -> Unit = {},
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Box(contentAlignment = Alignment.Center) {
        // Yumuşak pembe parıltı (görseldeki hâle).
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) LyraIcons.Pause else LyraIcons.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(36.dp),
                )
            }
        }
    }
}

/** Alt bar: cihazlara yayınla / Arkaplan / çalma sırası (görsel; kapsam dışı, no-op). */
@Composable
private fun BottomBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LyraIcons.Cast,
            contentDescription = "Cihazlar",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = LyraIcons.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Arkaplan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = LyraIcons.QueueMusic,
            contentDescription = "Çalma sırası",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) { Text("Tekrar dene") }
    }
}

/** "m:ss" biçimi; negatif/bilinmeyen süreler 0:00 olur. */
private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/** [id]'nin hash'inden stabil bir renk türetir (Feed/PlaylistDetail ile aynı yaklaşım). */
private fun artworkColorFor(id: String): Color {
    val hue = (((id.hashCode() % 360) + 360) % 360).toFloat()
    return Color.hsv(hue = hue, saturation = 0.5f, value = 0.6f)
}

private val previewState = PlayerUiState(
    songId = "s_neon-tide",
    title = "Neon Sokaklar",
    artist = "Şehir Işıkları",
    isPlaying = true,
    positionMs = 93_000L,
    durationMs = 223_000L,
    isLoading = false,
)

@Preview(name = "Player • Dark", showBackground = true)
@Composable
private fun PlayerScreenPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PlayerScreen(uiState = previewState, onIntent = {}, onNavigateBack = {})
        }
    }
}

@Preview(name = "Player • Error", showBackground = true)
@Composable
private fun PlayerScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PlayerScreen(
                uiState = previewState.copy(
                    isLoading = false,
                    errorMessage = "Şarkı yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}
