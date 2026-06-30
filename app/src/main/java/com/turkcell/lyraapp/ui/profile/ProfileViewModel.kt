package com.turkcell.lyraapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.UserStore
import com.turkcell.lyraapp.data.auth.resolveDisplayName
import com.turkcell.lyraapp.data.auth.resolveInitials
import com.turkcell.lyraapp.data.membership.MembershipStore
import com.turkcell.lyraapp.ui.theme.AppThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Profil ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [ProfileUiState]'i tek bir [StateFlow] üzerinden yayınlar. İstatistik içeriği statiktir; dinamik
 * alanlar üç app-scoped kaynaktan türetilir:
 * - [AppThemeController] → [ProfileUiState.isDarkTheme] ("Görünüm" toggle'ı uygulama temasını yansıtır).
 * - [MembershipStore] → [ProfileUiState.isPremium] + header alt satırı ([ProfileUiState.membership]).
 *   Tier kaynağı API'dir; istemci hesaplamaz, yalnızca aynalar (§2.2). Banner premium/free'ye göre değişir.
 * - [UserStore] → [ProfileUiState.displayName] + [ProfileUiState.initials]. Kullanıcının register'da
 *   girdiği ad/soyaddan türetilir (kaynak API; §2.2 — istemci uydurmaz, yalnızca biçimlendirir).
 * Gelen [ProfileIntent]'ler [onIntent] içinde reducer mantığıyla işlenir.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appThemeController: AppThemeController,
    private val membershipStore: MembershipStore,
    private val userStore: UserStore,
) : ViewModel() {

    // Tema henüz seçilmemişse (null = sistem), toggle'da varsayılan olarak "Açık" vurgulanır.
    // Tier (free/premium) MembershipStore'dan, kimlik (ad/baş harf) UserStore'dan aynalanır.
    val uiState: StateFlow<ProfileUiState> = combine(
        appThemeController.darkTheme,
        membershipStore.isPremiumFlow,
        userStore.userFlow,
    ) { dark, isPremium, user ->
        val name = user.resolveDisplayName() ?: ProfileUiState().displayName
        ProfileUiState(
            displayName = name,
            initials = resolveInitials(name) ?: ProfileUiState().initials,
            isDarkTheme = dark ?: false,
            isPremium = isPremium,
            membership = if (isPremium) "Premium · 3 gün" else "Ücretsiz",
        )
    }.stateIn(
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
