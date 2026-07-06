package com.turkcell.lyraapp.ui.createplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
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
 * Yeni çalma listesi ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [CreatePlaylistViewModel]'den toplar; kapatma (X) tıklamasını callback ile dışarı
 * bildirir (PlaylistDetailScreen deseni; `NavController` sızmaz). Görsel içerik, Hilt'siz de
 * önizlenebilmesi için ayrı bir stateless composable'a devredilir.
 */
@Composable
fun CreatePlaylistScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatePlaylistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CreatePlaylistEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    CreatePlaylistScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

/** Ekranın stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri [onIntent]/[onNavigateBack] ile bildirir. */
@Composable
private fun CreatePlaylistScreen(
    uiState: CreatePlaylistUiState,
    onIntent: (CreatePlaylistIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        CreatePlaylistTopBar(
            onNavigateBack = onNavigateBack,
            canSave = uiState.canSave,
            isSaving = uiState.isSaving,
            onSave = { onIntent(CreatePlaylistIntent.SaveClicked) },
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(Modifier.height(16.dp))

        // Başlık (kapak + ad/açıklama), "Herkese açık" ve "Şarkı ekle" başlığı şarkı listesiyle
        // birlikte kayar; yalnızca şarkı alanı yükleniyor/hata/boş durumuna göre değişir.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        ) {
            item {
                PlaylistHeader(
                    name = uiState.name,
                    description = uiState.description,
                    onNameChange = { onIntent(CreatePlaylistIntent.NameChanged(it)) },
                    onDescriptionChange = { onIntent(CreatePlaylistIntent.DescriptionChanged(it)) },
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                PublicToggleRow(
                    isPublic = uiState.isPublic,
                    onToggle = { onIntent(CreatePlaylistIntent.TogglePublic) },
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                AddSongsHeader(selectedCount = uiState.selectedCount)
                Spacer(Modifier.height(8.dp))
            }

            when {
                uiState.isLoading -> item { LoadingState() }
                uiState.errorMessage != null -> item {
                    ErrorState(
                        message = uiState.errorMessage,
                        onRetry = { onIntent(CreatePlaylistIntent.Refresh) },
                    )
                }

                uiState.songs.isEmpty() -> item { EmptyState() }
                else -> items(uiState.songs, key = { it.id }) { song ->
                    SelectableSongRow(
                        song = song,
                        isSelected = song.id in uiState.selectedSongIds,
                        onClick = { onIntent(CreatePlaylistIntent.ToggleSongSelection(song.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistTopBar(
    onNavigateBack: () -> Unit,
    canSave: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = LyraIcons.Close,
                contentDescription = "Kapat",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Yeni çalma listesi",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        SaveButton(enabled = canSave, isSaving = isSaving, onClick = onSave)
    }
}

/**
 * "Kaydet" düğmesi: ad girildiğinde marka (primary) renginde ve tıklanabilir, aksi halde muted/pasif.
 * Kaydetme sırasında yerine dönen bir gösterge çizer.
 */
@Composable
private fun SaveButton(
    enabled: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) scheme.primary else scheme.surfaceContainerHighest,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = scheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Text(
                    text = "Kaydet",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaylistCoverEditor(modifier = Modifier.size(96.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            UnderlinedField(
                value = name,
                onValueChange = onNameChange,
                placeholder = "Çalma listesi adı",
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                showUnderline = true,
            )
            Spacer(Modifier.height(8.dp))
            UnderlinedField(
                value = description,
                onValueChange = onDescriptionChange,
                placeholder = "Açıklama ekle",
                textStyle = MaterialTheme.typography.bodyMedium,
                showUnderline = false,
            )
        }
    }
}

/**
 * Kapak yer tutucu + düzenleme rozeti.
 *
 * API'da kapak görseli/rengi yoktur (§2.2); kapak temadan türetilmiş sıcak bir gradient'tir.
 * Kalem rozeti salt görseldir (kapak seçme işlevi talep edilmedi, §4.6).
 */
@Composable
private fun PlaylistCoverEditor(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(scheme.tertiary, lerp(scheme.tertiary, scheme.surface, 0.3f)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .padding(6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(scheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = LyraIcons.Edit,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Alt çizgili metin alanı (Material3 `TextField`'in görsel ağırlığı olmadan).
 *
 * Boşken [placeholder] gösterir; [showUnderline] ise altına marka (primary) ince çizgisini çizer
 * (ad alanı için ekran görüntüsündeki pembe çizgi).
 */
@Composable
private fun UnderlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    showUnderline: Boolean,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle.copy(color = scheme.onSurface),
            cursorBrush = SolidColor(scheme.primary),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = scheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            },
        )
        if (showUnderline) {
            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = scheme.primary, thickness = 2.dp)
        }
    }
}

@Composable
private fun PublicToggleRow(
    isPublic: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = LyraIcons.Public,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "Herkese açık",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Profilinde görünür",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Switch varsayılan renkleri seçili durumda primary'yi (marka pembesi) kullanır.
        Switch(checked = isPublic, onCheckedChange = { onToggle() })
    }
}

@Composable
private fun AddSongsHeader(
    selectedCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Şarkı ekle",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$selectedCount seçili",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

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
        Icon(
            imageVector = if (isSelected) LyraIcons.CheckCircle else LyraIcons.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Seçili" else "Seç",
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline
            },
            modifier = Modifier.size(24.dp),
        )
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
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
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
    Song("s_gece-yarisi", "Gece Yarısı", "Mavi Deniz", null, 0),
    Song("s_sessiz-sehir", "Sessiz Şehir", "Ela Tuna", null, 0),
    Song("s_yildiz-tozu", "Yıldız Tozu", "Polaris", null, 0),
    Song("s_sahil-yolu", "Sahil Yolu", "Kumsal", null, 0),
    Song("s_mor-bulutlar", "Mor Bulutlar", "Derin Kaya", null, 0),
    Song("s_ilk-isik", "İlk Işık", "Sabah Ezgisi", null, 0),
    Song("s_kayip-anlar", "Kayıp Anlar", "Eko", null, 0),
)

@Preview(name = "CreatePlaylist • Dark", showBackground = true)
@Composable
private fun CreatePlaylistScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatePlaylistScreen(
                uiState = CreatePlaylistUiState(
                    songs = previewSongs,
                    selectedSongIds = setOf("s_yildiz-tozu", "s_ilk-isik"),
                    isLoading = false,
                ),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}

@Preview(name = "CreatePlaylist • Light", showBackground = true)
@Composable
private fun CreatePlaylistScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatePlaylistScreen(
                uiState = CreatePlaylistUiState(songs = previewSongs, isLoading = false),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}

@Preview(name = "CreatePlaylist • Error", showBackground = true)
@Composable
private fun CreatePlaylistScreenErrorPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CreatePlaylistScreen(
                uiState = CreatePlaylistUiState(
                    isLoading = false,
                    errorMessage = "Şarkılar yüklenemedi. Lütfen tekrar deneyin.",
                ),
                onIntent = {},
                onNavigateBack = {},
            )
        }
    }
}
