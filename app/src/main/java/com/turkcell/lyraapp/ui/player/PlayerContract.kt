package com.turkcell.lyraapp.ui.player

/**
 * Player ekranının MVI sözleşmesi.
 *
 * Oynatma kontrolleri (oynat/durdur, seek/ilerlet) doğrudan ExoPlayer'a bağlı `PlayerView`
 * tarafından yönetilir; bu yüzden [PlayerUiState] yalnızca yükleme/hata durumunu tutar.
 */
data class PlayerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface PlayerIntent {
    data object Retry : PlayerIntent
}
