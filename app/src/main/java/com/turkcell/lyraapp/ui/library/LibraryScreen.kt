package com.turkcell.lyraapp.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.turkcell.lyraapp.data.playlist.Playlist
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Kütüphane ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [LibraryViewModel]'den toplar ve kullanıcı aksiyonlarını [LibraryIntent] olarak
 * geri iletir. Görsel içerik, Hilt'siz de önizlenebilmesi için ayrı bir stateless
 * composable'a ([LibraryScreen]) devredilir.
 */
@Composable
fun LibraryScreen(
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Ekran her öne geldiğinde (oluşturma/detaydan dönüş) listeyi sessizce tazele.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(LibraryIntent.ScreenResumed)
    }
    LibraryScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onPlaylistClick = onPlaylistClick,
        onCreatePlaylistClick = onCreatePlaylistClick,
        modifier = modifier,
    )
}

/** Kütüphane ekranının stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri [onIntent] ile bildirir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    uiState: LibraryUiState,
    onIntent: (LibraryIntent) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        LibraryTopBar(
            onCreatePlaylistClick = onCreatePlaylistClick,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(16.dp))

        LibraryFilterChips(modifier = Modifier.padding(horizontal = 24.dp))

        Spacer(Modifier.height(16.dp))

        LibrarySortRow(
            sortLabel = uiState.sortOrder.label,
            viewMode = uiState.viewMode,
            onToggleSort = { onIntent(LibraryIntent.ToggleSort) },
            onToggleViewMode = { onIntent(LibraryIntent.ToggleViewMode) },
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(8.dp))

        // İçerik alanı: yükleniyor / hata / boş / liste|ızgara durumları (FeedScreen deseni).
        // "Beğenilen Şarkılar" sayısı favori değişiminde ViewModel tarafından canlı güncellenir
        // (pull-to-refresh kaldırıldı — dinamik yapı).
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage,
                    onRetry = { onIntent(LibraryIntent.Refresh) },
                )

                uiState.playlists.isEmpty() -> EmptyState()
                uiState.viewMode == LibraryViewMode.LIST ->
                    PlaylistList(
                        playlists = uiState.playlists,
                        onPlaylistClick = onPlaylistClick,
                        onRequestDelete = { onIntent(LibraryIntent.RequestDeletePlaylist(it)) },
                    )

                else -> PlaylistGrid(
                    playlists = uiState.playlists,
                    onPlaylistClick = onPlaylistClick,
                    onRequestDelete = { onIntent(LibraryIntent.RequestDeletePlaylist(it)) },
                )
            }
        }
    }

    // Owned liste silme onayı (kalıcı silme).
    uiState.pendingDelete?.let { target ->
        DeleteConfirmDialog(
            playlistName = target.name,
            isDeleting = uiState.isDeleting,
            onConfirm = { onIntent(LibraryIntent.ConfirmDeletePlaylist) },
            onDismiss = { onIntent(LibraryIntent.DismissDeleteDialog) },
        )
    }
}

/** Owned çalma listesini kalıcı silmeden önce onay isteyen dialog. */
@Composable
private fun DeleteConfirmDialog(
    playlistName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Çalma listesini sil") },
        text = { Text("\"$playlistName\" listesi ve içindeki tüm şarkılar kalıcı olarak silinecek.") },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text("Sil", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Vazgeç")
            }
        },
    )
}

@Composable
private fun LibraryTopBar(
    onCreatePlaylistClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Kütüphane",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        // Salt görsel: arama aksiyonu talep edilmedi (§4.6).
        IconButton(onClick = {}) {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = "Ara",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        // İşlevsel: yeni çalma listesi oluşturma ekranını açar (kullanıcı kararı).
        IconButton(onClick = onCreatePlaylistClick) {
            Icon(
                imageVector = LyraIcons.Add,
                contentDescription = "Çalma listesi ekle",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Filtre çipleri (salt görsel): seçili sekme sabittir, tıklama state değiştirmez. */
@Composable
private fun LibraryFilterChips(modifier: Modifier = Modifier) {
    val selected = LibraryFilter.PLAYLISTS
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LibraryFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            FilterChip(
                selected = isSelected,
                onClick = {},
                label = { Text(filter.label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = LyraIcons.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
private fun LibrarySortRow(
    sortLabel: String,
    viewMode: LibraryViewMode,
    onToggleSort: () -> Unit,
    onToggleViewMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // İşlevsel: sıralama düzenini değiştirir (kullanıcı kararı).
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onToggleSort)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = LyraIcons.SwapVert,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = sortLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.weight(1f))

        // İşlevsel: liste ↔ ızgara görünümü arasında geçiş yapar (kullanıcı kararı).
        IconButton(onClick = onToggleViewMode) {
            Icon(
                imageVector = if (viewMode == LibraryViewMode.LIST) {
                    LyraIcons.GridView
                } else {
                    LyraIcons.ViewList
                },
                contentDescription = if (viewMode == LibraryViewMode.LIST) {
                    "Izgara görünüme geç"
                } else {
                    "Liste görünüme geç"
                },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<Playlist>,
    onPlaylistClick: (String) -> Unit,
    onRequestDelete: (Playlist) -> Unit,
) {
    val palettes = playlistPalettes()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        itemsIndexed(playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
            PlaylistRow(
                playlist = playlist,
                palette = palettes[index % palettes.size],
                onClick = { onPlaylistClick(playlist.id) },
                onRequestDelete = { onRequestDelete(playlist) },
            )
        }
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    palette: PlaylistPalette,
    onClick: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistArtwork(
            palette = palette,
            isLiked = playlist.isLiked,
            modifier = Modifier.size(56.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = playlist.subtitle(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        // Owned listede işlevsel "⋮" menüsü (Sil); sabitlenende iğne, diğerlerinde salt görsel ikon.
        when {
            playlist.isPinned -> Icon(
                imageVector = LyraIcons.PushPin,
                contentDescription = "Sabitlenmiş",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
            )

            playlist.isOwned -> {
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = LyraIcons.MoreVert,
                            contentDescription = "Daha fazla",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Çalma listesini sil") },
                            onClick = {
                                menuExpanded = false
                                onRequestDelete()
                            },
                        )
                    }
                }
            }

            else -> Icon(
                imageVector = LyraIcons.MoreVert,
                contentDescription = "Daha fazla",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun PlaylistGrid(
    playlists: List<Playlist>,
    onPlaylistClick: (String) -> Unit,
    onRequestDelete: (Playlist) -> Unit,
) {
    val palettes = playlistPalettes()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        gridItemsIndexed(playlists, key = { _, playlist -> playlist.id }) { index, playlist ->
            PlaylistGridCard(
                playlist = playlist,
                palette = palettes[index % palettes.size],
                onClick = { onPlaylistClick(playlist.id) },
                onRequestDelete = { onRequestDelete(playlist) },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistGridCard(
    playlist: Playlist,
    palette: PlaylistPalette,
    onClick: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            // Owned listede uzun basış silme onayını açar; diğerlerinde yalnızca normal tıklama.
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (playlist.isOwned) onRequestDelete else null,
            ),
    ) {
        PlaylistArtwork(
            palette = palette,
            isLiked = playlist.isLiked,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = playlist.subtitle(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** "Çalma listesi · N şarkı" alt başlığı. */
private fun Playlist.subtitle(): String = "Çalma listesi · $songCount şarkı"

/**
 * Kapak yer tutucu: temadan türetilmiş gradient. Beğenilen liste ([isLiked]) için ortada
 * kalp simgesi gösterilir (ekran görüntüsü referansı).
 */
@Composable
private fun PlaylistArtwork(
    palette: PlaylistPalette,
    isLiked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom))),
        contentAlignment = Alignment.Center,
    ) {
        if (isLiked) {
            Icon(
                imageVector = LyraIcons.Favorite,
                contentDescription = null,
                tint = palette.onColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
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
            text = "Henüz çalma listen yok",
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

/** Tema renklerinden türetilen kapak paleti: gradient durakları + okunaklı simge rengi. */
private data class PlaylistPalette(
    val top: Color,
    val bottom: Color,
    val onColor: Color,
)

/**
 * Kapak kartları için temadan türetilmiş palet listesi (SearchScreen.genrePalettes deseni).
 *
 * Her palet (rol → on-rol) çiftine dayanır; gradient'in ikinci durağı aynı rengin yüzeye
 * doğru hafif karıştırılmasıyla ([lerp]) üretilir. Böylece renkler markaya sabit paletten gelir
 * ve hem light hem dark temada okunaklı kalır. İlk (index 0) palet sabitli "Beğenilen Şarkılar"a
 * denk gelir ve marka primary tonunu alır (ekran görüntüsündeki vurgulu kapak).
 */
@Composable
private fun playlistPalettes(): List<PlaylistPalette> {
    val scheme = MaterialTheme.colorScheme
    fun palette(base: Color, on: Color) =
        PlaylistPalette(top = base, bottom = lerp(base, scheme.surface, 0.28f), onColor = on)
    return listOf(
        palette(scheme.primary, scheme.onPrimary),
        palette(scheme.tertiary, scheme.onTertiary),
        palette(scheme.secondary, scheme.onSecondary),
        palette(scheme.primaryContainer, scheme.onPrimaryContainer),
        palette(scheme.tertiaryContainer, scheme.onTertiaryContainer),
        palette(scheme.secondaryContainer, scheme.onSecondaryContainer),
    )
}

private val previewPlaylists = listOf(
    Playlist("liked", "Beğenilen Şarkılar", null, songCount = 12, isLiked = true, isPinned = true),
    Playlist("p_night-drive", "Gece Sürüşü", null, songCount = 6),
    Playlist("p_morning", "Sabah Kahvesi", null, songCount = 5),
    Playlist("p_focus", "Odaklan", null, songCount = 5),
    Playlist("p_summer", "Yaz Anıları", null, songCount = 5),
    Playlist("p_acoustic", "Akustik Akşam", null, songCount = 4),
)

@Preview(name = "Library • List Dark", showBackground = true)
@Composable
private fun LibraryScreenListDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LibraryScreen(
                uiState = LibraryUiState(playlists = previewPlaylists, isLoading = false),
                onIntent = {},
                onPlaylistClick = {},
                onCreatePlaylistClick = {},
            )
        }
    }
}

@Preview(name = "Library • List Light", showBackground = true)
@Composable
private fun LibraryScreenListLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LibraryScreen(
                uiState = LibraryUiState(playlists = previewPlaylists, isLoading = false),
                onIntent = {},
                onPlaylistClick = {},
                onCreatePlaylistClick = {},
            )
        }
    }
}

@Preview(name = "Library • Grid Dark", showBackground = true)
@Composable
private fun LibraryScreenGridDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LibraryScreen(
                uiState = LibraryUiState(
                    playlists = previewPlaylists,
                    viewMode = LibraryViewMode.GRID,
                    isLoading = false,
                ),
                onIntent = {},
                onPlaylistClick = {},
                onCreatePlaylistClick = {},
            )
        }
    }
}

@Preview(name = "Library • Error", showBackground = true)
@Composable
private fun LibraryScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LibraryScreen(
                uiState = LibraryUiState(
                    isLoading = false,
                    errorMessage = "Çalma listeleri yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onPlaylistClick = {},
                onCreatePlaylistClick = {},
            )
        }
    }
}
