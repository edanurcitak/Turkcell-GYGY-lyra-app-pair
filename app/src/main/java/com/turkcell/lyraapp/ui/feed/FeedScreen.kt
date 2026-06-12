package com.turkcell.lyraapp.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.turkcell.lyraapp.data.feed.ArtworkTone
import com.turkcell.lyraapp.data.feed.MediaCard
import com.turkcell.lyraapp.data.feed.QuickPick
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Ana sayfa (feed) ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [FeedViewModel]'den toplar, görsel içeriği stateless gövdeye devreder.
 */
@Composable
fun FeedScreen(modifier: Modifier = Modifier, viewModel: FeedViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedScreen(uiState = uiState, onIntent = viewModel::onIntent, modifier = modifier)
}

/** Feed ekranının stateless gövdesi. */
@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onIntent: (FeedIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
    ) {
        Header(
            greeting = uiState.greeting,
            userInitials = uiState.userInitials,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(24.dp))

        QuickPickGrid(
            items = uiState.quickPicks,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(28.dp))

        SectionHeader(
            title = "Son çalınanlar",
            actionLabel = "Tümü",
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(12.dp))
        MediaCardRow(items = uiState.recentlyPlayed)

        Spacer(Modifier.height(28.dp))

        SectionHeader(
            title = "Senin için çalma listeleri",
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(12.dp))
        MediaCardRow(items = uiState.playlists)

        Spacer(Modifier.height(16.dp))
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
private fun QuickPickGrid(
    items: List<QuickPick>,
    modifier: Modifier = Modifier,
) {
    // İç içe dikey scroll çakışmasını önlemek için LazyVerticalGrid yerine manuel 2 sütun.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    QuickPickTile(item = item, modifier = Modifier.weight(1f))
                }
                // Tek kalan eleman olursa hizayı korumak için boş ağırlık.
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickPickTile(
    item: QuickPick,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Artwork(
                tone = item.tone,
                modifier = Modifier
                    .padding(6.dp)
                    .size(44.dp),
                cornerRadius = 8.dp,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (actionLabel != null) {
            // "Tümünü gör" navigasyonu kapsam dışı; şimdilik statik.
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MediaCardRow(items: List<MediaCard>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(items, key = { it.id }) { card ->
            MediaCardItem(card)
        }
    }
}

@Composable
private fun MediaCardItem(card: MediaCard) {
    Column(modifier = Modifier.width(150.dp)) {
        Artwork(
            tone = card.tone,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            cornerRadius = 16.dp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = card.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = card.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Asset olmadığından kapak görseli yerine tema tonundan türetilen renkli yer tutucu. */
@Composable
private fun Artwork(
    tone: ArtworkTone,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(tone.toContainerColor()),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = LyraIcons.Waveform,
            contentDescription = null,
            tint = tone.toOnContainerColor().copy(alpha = 0.45f),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun ArtworkTone.toContainerColor(): Color = when (this) {
    ArtworkTone.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    ArtworkTone.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    ArtworkTone.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
    ArtworkTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHighest
}

@Composable
private fun ArtworkTone.toOnContainerColor(): Color = when (this) {
    ArtworkTone.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    ArtworkTone.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    ArtworkTone.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
    ArtworkTone.NEUTRAL -> MaterialTheme.colorScheme.onSurface
}

private val previewFeed = FeedUiState(
    greeting = "İyi akşamlar",
    userInitials = "ZK",
    quickPicks = listOf(
        QuickPick("1", "Gece Sürüşü", ArtworkTone.PRIMARY),
        QuickPick("2", "Sabah Kahvaltısı", ArtworkTone.TERTIARY),
        QuickPick("3", "Neon Sokaklar", ArtworkTone.SECONDARY),
        QuickPick("4", "Odaklan", ArtworkTone.NEUTRAL),
    ),
    recentlyPlayed = listOf(
        MediaCard("r1", "Neon Sokaklar", "Şehir Işıkları", ArtworkTone.SECONDARY),
        MediaCard("r2", "Derin Mavi", "Okyanus", ArtworkTone.PRIMARY),
    ),
    playlists = listOf(
        MediaCard("p1", "Akşam Sakinliği", "Lyra Mix", ArtworkTone.NEUTRAL),
        MediaCard("p2", "Yoğunlaşma", "Lo-Fi seçkisi", ArtworkTone.PRIMARY),
    ),
    isLoading = false,
)

@Preview(name = "Feed • Dark", showBackground = true)
@Composable
private fun FeedScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(uiState = previewFeed, onIntent = {})
        }
    }
}

@Preview(name = "Feed • Light", showBackground = true)
@Composable
private fun FeedScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            FeedScreen(uiState = previewFeed, onIntent = {})
        }
    }
}
