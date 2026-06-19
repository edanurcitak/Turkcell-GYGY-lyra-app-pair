package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.ui.theme.AppThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Profil ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [ProfileUiState]'i tek bir [StateFlow] üzerinden yayınlar. Profil/istatistik içeriği statiktir;
 * tek dinamik alan, app-scoped [AppThemeController]'dan türetilen [ProfileUiState.isDarkTheme]'dir.
 * Böylece "Görünüm" toggle'ı, uygulama genelindeki tema durumunu yansıtır.
 * Gelen [ProfileIntent]'ler [onIntent] içinde reducer mantığıyla işlenir.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appThemeController: AppThemeController,
) : ViewModel() {

    // Tema henüz seçilmemişse (null = sistem), toggle'da varsayılan olarak "Açık" vurgulanır.
    val uiState: StateFlow<ProfileUiState> = appThemeController.darkTheme
        .map { dark -> ProfileUiState(isDarkTheme = dark ?: false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.ThemeChanged ->
                appThemeController.setDarkTheme(intent.darkTheme)
        }
    }
}
