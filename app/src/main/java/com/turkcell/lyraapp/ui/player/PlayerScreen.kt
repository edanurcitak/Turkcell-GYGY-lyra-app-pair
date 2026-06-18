package com.turkcell.lyraapp.ui.player

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.turkcell.lyraapp.ui.icons.LyraIcons

/**
 * Player ekranı — stateful giriş noktası (AGENTS.MD §4.5).
 *
 * Tasarım kasıtlı olarak minimaldir; tüm oynatma kontrolleri Media3 [PlayerView] tarafından
 * sağlanır (oynat/durdur, seek bar ile ilerlet, geri-sar/yeniden başlat). Player örneği
 * [PlayerViewModel]'de yaşar; ekran yalnızca ona bağlanır.
 */
@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val errorMessage = uiState.errorMessage
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = LyraIcons.ArrowBack,
                contentDescription = "Geri",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isLoading ->
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)

                errorMessage != null -> ErrorState(
                    message = errorMessage,
                    onRetry = { viewModel.onIntent(PlayerIntent.Retry) },
                )

                else -> PlayerSurface(viewModel)
            }
        }
    }
}

/** ExoPlayer kontrollerini barındıran PlayerView (kontroller sürekli görünür). */
@OptIn(UnstableApi::class)
@Composable
private fun PlayerSurface(viewModel: PlayerViewModel) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = viewModel.player
                useController = true
                controllerShowTimeoutMs = 0      // kontroller otomatik gizlenmesin
                controllerHideOnTouch = false
            }
        },
        onRelease = { it.player = null },
    )
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
