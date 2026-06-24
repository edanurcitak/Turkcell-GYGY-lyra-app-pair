package com.turkcell.lyraapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.ui.theme.AppThemeController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Ana sayfa (feed) ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Üç kişiselleştirilmiş bölümü ([FeedUiState.recommendations] / [FeedUiState.recentlyPlayed] /
 * [FeedUiState.forYou]) [SongRepository] üzerinden **eşzamanlı** yükler ve tek bir [StateFlow] ile
 * [FeedUiState] yayınlar. Başlıktaki tema düğmesi app-scoped [AppThemeController]'a yazar; aynı
 * tutucudan okunan koyu/açık durumu [FeedUiState.isDarkTheme]'e yansıtılır. Bağımlılıklar Hilt
 * tarafından constructor ile enjekte edilir.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val appThemeController: AppThemeController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        observeTheme()
        load()
    }

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            FeedIntent.Refresh -> load()
            is FeedIntent.ToggleTheme -> appThemeController.setDarkTheme(intent.darkTheme)
        }
    }

    /** App-scoped tema durumunu izler (null = sistem → açık olarak gösterilir). */
    private fun observeTheme() {
        viewModelScope.launch {
            appThemeController.darkTheme.collect { dark ->
                _uiState.update { it.copy(isDarkTheme = dark ?: false) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Üç bölüm birbirinden bağımsız; paralel çekilip tek seferde state'e yazılır.
                val recentlyPlayed = async { songRepository.getRecentlyPlayed() }
                val forYou = async { songRepository.getForYou() }
                val recommendations = async { songRepository.getRecommendations() }
                _uiState.update {
                    it.copy(
                        recentlyPlayed = recentlyPlayed.await(),
                        forYou = forYou.await(),
                        recommendations = recommendations.await(),
                        isLoading = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "İçerik yüklenemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }
}
