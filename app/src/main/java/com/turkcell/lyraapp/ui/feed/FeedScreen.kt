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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

                uiState.songs.isEmpty() -> EmptyState()
                else -> SongList(
                    songs = uiState.songs,
                    onSongClick = onSongClick,
                )
            }
        }
    }
}

@Composable
private fun Header(
    greeting: String,
    userInitials: String,
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

@Composable
private fun SongList(
    songs: List<Song>,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Text(
                text = "Şarkılar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        items(songs, key = { it.id }) { song ->
            SongRow(song = song, onClick = { onSongClick(song.id, song.title, song.artist) })
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SongArtwork(
            songId = song.id,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
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
}

/** "sanatçı · albüm" alt başlığı; albüm yoksa yalnızca sanatçı. */
private fun Song.subtitle(): String =
    if (album.isNullOrBlank()) artist else "$artist · $album"

/**
 * Kapak yer tutucu.
 *
 * API'da şarkı için renk olmadığından (§2.2), renk [songId]'den deterministik üretilir:
 * aynı şarkı her zaman aynı rengi alır, recomposition'da titremez.
 */
@Composable
private fun SongArtwork(
    songId: String,
    modifier: Modifier = Modifier,
) {
    val color = remember(songId) { artworkColorFor(songId) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LyraIcons.Waveform,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(24.dp),
        )
    }
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
            text = "Henüz şarkı yok",
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
    Song("s_neon-tide", "Neon Tide", "Aurora Drift", "City Lights", 32000),
    Song("s_midnight", "Midnight Avenue", "Echo Park", null, 41000),
    Song("s_deep-blue", "Derin Mavi", "Okyanus", "Mavi", 28000),
    Song("s_polaris", "Yıldız Tozu", "Polaris", "Kuzey", 36000),
)

@Preview(name = "Feed • Dark", showBackground = true)
@Composable
private fun FeedScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(
                uiState = FeedUiState(songs = previewSongs, isLoading = false),
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
                uiState = FeedUiState(songs = previewSongs, isLoading = false),
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
                    errorMessage = "Şarkılar yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}
