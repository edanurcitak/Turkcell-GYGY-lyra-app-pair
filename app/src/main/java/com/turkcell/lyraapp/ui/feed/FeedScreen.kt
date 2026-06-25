package com.turkcell.lyraapp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.data.feed.Song
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Ana sayfa (feed) ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [FeedViewModel]'den toplar, görsel içeriği stateless gövdeye devreder.
 */
@Composable
fun FeedScreen(
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Ekran her öne geldiğinde (sekme dönüşü / player'dan dönüş) içeriği tazele; böylece yeni
    // çalmalar "Son çalınanlar"a yansır. Tazeleme sessizdir — mevcut içerik gizlenmez (bkz. VM).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(FeedIntent.Refresh)
    }
    FeedScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onSongClick = onSongClick,
        modifier = modifier,
    )
}

/** Feed ekranının stateless gövdesi. */
@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onIntent: (FeedIntent) -> Unit,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        Header(
            greeting = uiState.greeting,
            userInitials = uiState.userInitials,
            isDarkTheme = uiState.isDarkTheme,
            onToggleTheme = { onIntent(FeedIntent.ToggleTheme(!uiState.isDarkTheme)) },
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(24.dp))

        // İçerik alanı: yükleniyor / hata / boş / liste durumları.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage,
                    onRetry = { onIntent(FeedIntent.Refresh) },
                )

                uiState.recommendations.isEmpty() &&
                    uiState.recentlyPlayed.isEmpty() &&
                    uiState.forYou.isEmpty() -> EmptyState()

                else -> FeedContent(uiState = uiState, onSongClick = onSongClick)
            }
        }
    }
}

@Composable
private fun Header(
    greeting: String,
    userInitials: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ne dinlemek istersin?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        // Görünüm düğmesi: gösterilen ikon, geçilecek modu temsil eder (koyuyken güneş, açıkken ay).
        IconButton(onClick = onToggleTheme) {
            Icon(
                imageVector = if (isDarkTheme) LyraIcons.LightMode else LyraIcons.DarkMode,
                contentDescription = if (isDarkTheme) "Açık temaya geç" else "Koyu temaya geç",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(4.dp))
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = userInitials,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Yüklü içerik: üstte "Önerilenler" ızgarası, ardından yatay kaydırmalı bölümler.
 *
 * Bölümler tek bir dikey [LazyColumn]'da; karuseller kenara taşabilsin diye yatay dolgu
 * bölüm başlıklarında ve [LazyRow] `contentPadding`'inde verilir, LazyColumn'a değil.
 */
@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        if (uiState.recommendations.isNotEmpty()) {
            item(key = "recommendations") {
                RecommendationSection(
                    songs = uiState.recommendations,
                    onSongClick = onSongClick,
                )
            }
        }
        if (uiState.recentlyPlayed.isNotEmpty()) {
            item(key = "recently-played") {
                SongCarousel(
                    title = "Son çalınanlar",
                    actionLabel = "Tümü",
                    songs = uiState.recentlyPlayed,
                    onSongClick = onSongClick,
                )
            }
        }
        if (uiState.forYou.isNotEmpty()) {
            item(key = "for-you") {
                SongCarousel(
                    title = "Senin için müzikler",
                    actionLabel = null,
                    songs = uiState.forYou,
                    onSongClick = onSongClick,
                )
            }
        }
    }
}

/** "Önerilenler" — 2 sütunlu kompakt kart ızgarası (en fazla 6 öğe). */
@Composable
private fun RecommendationSection(
    songs: List<Song>,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
) {
    Column(Modifier.padding(horizontal = 24.dp)) {
        SectionTitle("Önerilenler")
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            songs.take(6).chunked(2).forEach { rowSongs ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowSongs.forEach { song ->
                        CompactSongCard(
                            song = song,
                            onClick = { onSongClick(song.id, song.title, song.artist) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Tek öğe kalan satırda hizayı korumak için boş yer.
                    if (rowSongs.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CompactSongCard(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(artworkBrush(song.id)),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )
    }
}

/** Başlık + (opsiyonel) "Tümü" + büyük kapaklı yatay liste. */
@Composable
private fun SongCarousel(
    title: String,
    actionLabel: String?,
    songs: List<Song>,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            // "Tümü": hedef ekran/uç olmadığından yalnızca görseldir (AGENTS.MD §4.6).
            if (actionLabel != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(songs, key = { it.id }) { song ->
                BigSongCard(
                    song = song,
                    onClick = { onSongClick(song.id, song.title, song.artist) },
                )
            }
        }
    }
}

@Composable
private fun BigSongCard(
    song: Song,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(artworkBrush(song.id)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Waveform,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = song.subtitle(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/** "sanatçı · albüm" alt başlığı; albüm yoksa yalnızca sanatçı. */
private fun Song.subtitle(): String =
    if (album.isNullOrBlank()) artist else "$artist · $album"

/**
 * Kapak yer tutucu gradyanı.
 *
 * API'da şarkı için kapak/renk olmadığından (§2.2), gradyan [id]'den deterministik üretilir:
 * aynı şarkı her zaman aynı rengi alır, recomposition'da titremez. Üç tonlu (açık→taban→koyu)
 * geçiş, Player/bildirim kapağıyla görsel olarak tutarlıdır.
 */
private fun artworkBrush(id: String): Brush {
    val base = artworkColorFor(id)
    return Brush.linearGradient(
        listOf(
            lerp(base, Color.White, 0.22f),
            base,
            lerp(base, Color.Black, 0.30f),
        ),
    )
}

/** [id]'nin hash'inden stabil bir renk türetir (rastgele görünür ama deterministiktir). */
private fun artworkColorFor(id: String): Color {
    val hue = (((id.hashCode() % 360) + 360) % 360).toFloat()
    return Color.hsv(hue = hue, saturation = 0.5f, value = 0.6f)
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Henüz içerik yok",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text("Tekrar dene")
        }
    }
}

private val previewSongs = listOf(
    Song("s_neon-tide", "Neon Sokaklar", "Şehir Işıkları", "City Lights", 32000),
    Song("s_deep-blue", "Derin Mavi", "Okyanus", "Mavi", 28000),
    Song("s_polaris", "Yıldız Tozu", "Polaris", "Kuzey", 36000),
    Song("s_dawn", "Sabah Kahvesi", "Lo-Fi Cafe", null, 30000),
    Song("s_focus", "Odaklan", "Deep Focus", null, 30000),
    Song("s_summer", "Yaz Anıları", "Retro Wave", null, 30000),
)

@Preview(name = "Feed • Dark", showBackground = true)
@Composable
private fun FeedScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(
                uiState = FeedUiState(
                    recommendations = previewSongs,
                    recentlyPlayed = previewSongs.take(4),
                    forYou = previewSongs.takeLast(4),
                    isDarkTheme = true,
                    isLoading = false,
                ),
                onIntent = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}

@Preview(name = "Feed • Light", showBackground = true)
@Composable
private fun FeedScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(
                uiState = FeedUiState(
                    recommendations = previewSongs,
                    recentlyPlayed = previewSongs.take(4),
                    forYou = previewSongs.takeLast(4),
                    isDarkTheme = false,
                    isLoading = false,
                ),
                onIntent = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}

@Preview(name = "Feed • Error", showBackground = true)
@Composable
private fun FeedScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(
                uiState = FeedUiState(
                    isLoading = false,
                    errorMessage = "İçerik yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}
