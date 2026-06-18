package com.turkcell.lyraapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.lyraapp.ui.icons.LyraIcons
import com.turkcell.lyraapp.ui.theme.LyraAppTheme

/**
 * Ara ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * State'i [SearchViewModel]'den toplar ve kullanıcı aksiyonlarını [SearchIntent] olarak
 * geri iletir. Görsel içerik, Hilt'siz de önizlenebilmesi için ayrı bir stateless
 * composable'a ([SearchScreen]) devredilir.
 */
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

/** Ara ekranının stateless gövdesi: yalnızca [uiState]'i çizer, etkileşimleri [onIntent] ile bildirir. */
@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = "Ara",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(16.dp))

        SearchField(
            value = uiState.query,
            onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Türlere göz at",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(16.dp))

        GenreGrid(
            genres = uiState.genres,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Şarkı, sanatçı veya albüm") },
        leadingIcon = {
            Icon(
                imageVector = LyraIcons.Search,
                contentDescription = null,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
    )
}

@Composable
private fun GenreGrid(
    genres: List<SearchGenre>,
    modifier: Modifier = Modifier,
) {
    val palettes = genrePalettes()
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(genres, key = { _, genre -> genre.id }) { index, genre ->
            GenreCard(
                name = genre.name,
                palette = palettes[index % palettes.size],
            )
        }
    }
}

@Composable
private fun GenreCard(
    name: String,
    palette: GenrePalette,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(palette.top, palette.bottom)))
            .padding(16.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = palette.onColor,
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

/** Tema renklerinden türetilen kart paleti: gradient durakları + okunaklı etiket rengi. */
private data class GenrePalette(
    val top: Color,
    val bottom: Color,
    val onColor: Color,
)

/**
 * 8 tür kartı için temadan türetilmiş palet listesi.
 *
 * Her palet, M3'ün okunabilirlik garantisi olan (rol → on-rol) çiftine dayanır; gradient'in
 * ikinci durağı aynı rengin yüzeye doğru hafif karıştırılmasıyla ([lerp]) üretilir. Böylece
 * etiket hem light hem dark temada okunaklı kalır ve renkler markaya sabit paletten gelir.
 */
@Composable
private fun genrePalettes(): List<GenrePalette> {
    val scheme = MaterialTheme.colorScheme
    fun palette(base: Color, on: Color) =
        GenrePalette(top = base, bottom = lerp(base, scheme.surface, 0.22f), onColor = on)
    return listOf(
        palette(scheme.primary, scheme.onPrimary),
        palette(scheme.secondary, scheme.onSecondary),
        palette(scheme.tertiary, scheme.onTertiary),
        palette(scheme.primaryContainer, scheme.onPrimaryContainer),
        palette(scheme.secondaryContainer, scheme.onSecondaryContainer),
        palette(scheme.tertiaryContainer, scheme.onTertiaryContainer),
        palette(scheme.surfaceContainerHighest, scheme.onSurface),
        palette(scheme.surfaceVariant, scheme.onSurfaceVariant),
    )
}

@Preview(name = "Search • Dark", showBackground = true)
@Composable
private fun SearchScreenDarkPreview() {
    LyraAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SearchScreen(uiState = SearchUiState(), onIntent = {})
        }
    }
}

@Preview(name = "Search • Light", showBackground = true)
@Composable
private fun SearchScreenLightPreview() {
    LyraAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            SearchScreen(uiState = SearchUiState(), onIntent = {})
        }
    }
}
