package com.turkcell.lyraapp.ui.miniplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Docked mini player — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [MiniPlayerViewModel]'den toplar; çubuğa dokunulduğunda [onExpand] ile aktif şarkıyı
 * tam ekran player'a açar (navigasyon callback üzerinden, mevcut konvansiyon).
 */
@Composable
fun MiniPlayer(
    onExpand: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MiniPlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MiniPlayer(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onExpand = onExpand,
        modifier = modifier,
    )
}

/** Mini player'ın stateless gövdesi. Aktif parça yokken hiçbir şey çizmez. */
@Composable
private fun MiniPlayer(
    uiState: MiniPlayerUiState,
    onIntent: (MiniPlayerIntent) -> Unit,
    onExpand: (songId: String, title: String, artist: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isVisible) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable { onExpand(uiState.songId, uiState.title, uiState.artist) }
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(artworkBrush(uiState.songId)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = LyraIcons.Waveform,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = uiState.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = { onIntent(MiniPlayerIntent.SkipPrevious) }) {
                Icon(
                    imageVector = LyraIcons.SkipPrevious,
                    contentDescription = "Önceki",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = { onIntent(MiniPlayerIntent.PlayPause) }) {
                Icon(
                    imageVector = if (uiState.isPlaying) LyraIcons.Pause else LyraIcons.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Duraklat" else "Oynat",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = { onIntent(MiniPlayerIntent.SkipNext) }) {
                Icon(
                    imageVector = LyraIcons.SkipNext,
                    contentDescription = "Sonraki",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Kapak yer tutucu gradyanı — [com.turkcell.lyraapp.ui.feed] ile aynı deterministik türetim.
 *
 * API'da şarkı kapağı yoktur (§2.2); renk [id]'nin hash'inden gelir, böylece aynı şarkı her yerde
 * (Feed/Player/mini player) aynı kapağı alır.
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

private fun artworkColorFor(id: String): Color {
    val hue = (((id.hashCode() % 360) + 360) % 360).toFloat()
    return Color.hsv(hue = hue, saturation = 0.5f, value = 0.6f)
}

@Preview(name = "MiniPlayer • Dark", showBackground = true)
@Composable
private fun MiniPlayerDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.padding(8.dp)) {
                MiniPlayer(
                    uiState = MiniPlayerUiState(
                        songId = "s_neon-tide",
                        title = "Neon Sokaklar",
                        artist = "Şehir Işıkları",
                        isPlaying = true,
                        isVisible = true,
                    ),
                    onIntent = {},
                    onExpand = { _, _, _ -> },
                )
            }
        }
    }
}
