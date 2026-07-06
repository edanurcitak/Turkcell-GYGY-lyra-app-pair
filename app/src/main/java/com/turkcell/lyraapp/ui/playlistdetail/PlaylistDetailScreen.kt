package com.turkcell.lyraapp.ui.playlistdetail

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
 * Çalma listesi detay ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [PlaylistDetailViewModel]'den toplar; geri ve şarkı tıklamalarını callback'lerle
 * dışarı bildirir (FeedScreen/PlayerScreen deseni; `NavController` sızmaz). Görsel içerik,
 * Hilt'siz de önizlenebilmesi için ayrı bir stateless composable'a devredilir.
 */
@Composable
fun PlaylistDetailScreen(
    onNavigateBack: () -> Unit,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlaylistDetailScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        onSongClick = onSongClick,
        modifier = modifier,
    )
}

/** Detay ekranının stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri dışarı bildirir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailScreen(
    uiState: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        DetailTopBar(
            title = uiState.title,
            songCount = uiState.songs.size,
            showSongCount = !uiState.isLoading && uiState.songs.isNotEmpty(),
            onNavigateBack = onNavigateBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(Modifier.height(8.dp))

        // Yalnızca kullanıcının kendi (owned) listesinde şarkı ekleme girişi gösterilir.
        if (uiState.isOwned) {
            AddSongsBar(
                onAdd = { onIntent(PlaylistDetailIntent.OpenAddSheet) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(4.dp))
        }

        // İçerik alanı: yükleniyor / hata / boş / liste durumları (FeedScreen deseni).
        // Aşağı çekince ([PlaylistDetailIntent.PullRefresh]) liste görünür kalarak üstte yenileme göstergesi döner.
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onIntent(PlaylistDetailIntent.PullRefresh) },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage,
                    onRetry = { onIntent(PlaylistDetailIntent.Retry) },
                )

                uiState.songs.isEmpty() -> EmptyState()
                else -> SongList(
                    songs = uiState.songs,
                    showRemove = uiState.isOwned,
                    onSongClick = onSongClick,
                    onRemove = { songId -> onIntent(PlaylistDetailIntent.RemoveSong(songId)) },
                )
            }
        }
    }

    // Şarkı ekleme sayfası (owned liste): katalogdan çok-seçimli ekleme.
    if (uiState.showAddSheet) {
        AddSongsSheet(uiState = uiState, onIntent = onIntent)
    }
}

/** "Şarkı ekle" girişi — yalnızca owned listede gösterilir (ekleme sayfasını açar). */
@Composable
private fun AddSongsBar(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(onClick = onAdd, modifier = modifier) {
        Icon(
            imageVector = LyraIcons.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Şarkı ekle",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Katalogdan çok-seçimli şarkı ekleme sayfası (ModalBottomSheet).
 *
 * Yalnızca listede **olmayan** şarkılar listelenir ([PlaylistDetailUiState.catalogSongs]); seçim
 * [PlaylistDetailIntent.ToggleAddSelection] ile, onay [PlaylistDetailIntent.ConfirmAddSongs] ile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSongsSheet(
    uiState: PlaylistDetailUiState,
    onIntent: (PlaylistDetailIntent) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = { onIntent(PlaylistDetailIntent.CloseAddSheet) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Şarkı ekle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${uiState.addSelectionCount} seçili",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))

            when {
                uiState.isCatalogLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                uiState.catalogSongs.isEmpty() -> Text(
                    text = "Eklenebilecek şarkı yok",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.catalogSongs, key = { it.id }) { song ->
                        SelectableSongRow(
                            song = song,
                            isSelected = song.id in uiState.selectedToAdd,
                            onClick = { onIntent(PlaylistDetailIntent.ToggleAddSelection(song.id)) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onIntent(PlaylistDetailIntent.ConfirmAddSongs) },
                enabled = uiState.addSelectionCount > 0 && !uiState.isAddingSongs,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isAddingSongs) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text("Ekle (${uiState.addSelectionCount})")
                }
            }
        }
    }
}

/** Ekleme sayfasındaki seçilebilir şarkı satırı (CreatePlaylist ekranındaki desenle aynı). */
@Composable
private fun SelectableSongRow(
    song: Song,
    isSelected: Boolean,
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
        SongArtwork(songId = song.id, modifier = Modifier.size(48.dp))
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
        Icon(
            imageVector = if (isSelected) LyraIcons.CheckCircle else LyraIcons.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Seçili" else "Seç",
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    songCount: Int,
    showSongCount: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(4.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showSongCount) {
                Text(
                    text = "$songCount şarkı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SongList(
    songs: List<Song>,
    showRemove: Boolean,
    onSongClick: (songId: String, title: String, artist: String) -> Unit,
    onRemove: (songId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                showRemove = showRemove,
                onClick = { onSongClick(song.id, song.title, song.artist) },
                onRemove = { onRemove(song.id) },
            )
        }
    }
}

@Composable
private fun SongRow(
    song: Song,
    showRemove: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
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
        // Owned listede satırdan çıkarma (X); diğer listelerde gizli.
        if (showRemove) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = LyraIcons.Close,
                    contentDescription = "Listeden çıkar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** "sanatçı · albüm" alt başlığı; albüm yoksa yalnızca sanatçı (FeedScreen ile aynı kural). */
private fun Song.subtitle(): String =
    if (album.isNullOrBlank()) artist else "$artist · $album"

/**
 * Kapak yer tutucu. API'da şarkı için renk olmadığından (§2.2), renk [songId]'den
 * deterministik üretilir: aynı şarkı her zaman aynı rengi alır (FeedScreen ile aynı yaklaşım).
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
            text = "Bu listede henüz şarkı yok",
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
)

@Preview(name = "PlaylistDetail • Dark", showBackground = true)
@Composable
private fun PlaylistDetailScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PlaylistDetailScreen(
                uiState = PlaylistDetailUiState(
                    title = "Gece Sürüşü",
                    songs = previewSongs,
                    isLoading = false,
                ),
                onIntent = {},
                onNavigateBack = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}

@Preview(name = "PlaylistDetail • Error", showBackground = true)
@Composable
private fun PlaylistDetailScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            PlaylistDetailScreen(
                uiState = PlaylistDetailUiState(
                    title = "Gece Sürüşü",
                    isLoading = false,
                    errorMessage = "Çalma listesi yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onNavigateBack = {},
                onSongClick = { _, _, _ -> },
            )
        }
    }
}
