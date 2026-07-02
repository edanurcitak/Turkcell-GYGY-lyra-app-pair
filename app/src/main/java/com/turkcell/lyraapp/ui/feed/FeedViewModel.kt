package com.turkcell.lyraapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.UserStore
import com.turkcell.lyraapp.data.auth.resolveDisplayName
import com.turkcell.lyraapp.data.auth.resolveInitials
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.ui.theme.AppThemeController
import com.turkcell.lyraapp.util.ErrorContext
import com.turkcell.lyraapp.util.toAppError
import com.turkcell.lyraapp.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
 * [FeedUiState] yayınlar.
 *
 * **Tazeleme:** İlk yükleme ile sonraki tazelemeler aynı [load] yolunu kullanır; ekran her öne
 * geldiğinde ([FeedIntent.Refresh]) tetiklenir, böylece yeni çalmalar "Son çalınanlar"a yansır.
 * İçerik zaten varsa tazeleme **sessizdir**: spinner gösterilmez ve başarısız tazelemede mevcut
 * içerik korunur (yalnızca ilk, boş yüklemede yüklenme/hata durumu gösterilir).
 *
 * Başlıktaki tema düğmesi app-scoped [AppThemeController]'a yazar; aynı tutucudan okunan koyu/açık
 * durumu [FeedUiState.isDarkTheme]'e yansıtılır. Bağımlılıklar Hilt ile constructor'dan enjekte edilir.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val appThemeController: AppThemeController,
    private val userStore: UserStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    // Üst üste binen tazelemeleri (hızlı RESUME'lar) önlemek için tek aktif yükleme işi.
    private var loadJob: Job? = null

    init {
        observeTheme()
        observeUser()
        // İlk yükleme ekranın ON_RESUME'unda ([FeedIntent.Refresh]) tetiklenir; burada çağrılmaz.
    }

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            FeedIntent.Refresh -> load()
            // Pull-to-refresh: göstergeyi hemen göster, ardından aynı yükleme yolunu kullan.
            FeedIntent.PullRefresh -> {
                _uiState.update { it.copy(isRefreshing = true) }
                load()
            }
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

    /**
     * App-scoped kullanıcı kimliğini izler; baş harfleri başlık avatarına ([FeedUiState.userInitials])
     * yansıtır. Kaynak [UserStore] (API'den aynalanır; §2.2 — istemci uydurmaz, yalnızca biçimlendirir);
     * türetilemezse nötr varsayılan ([FeedUiState] başlangıcı) korunur.
     */
    private fun observeUser() {
        viewModelScope.launch {
            userStore.userFlow.collect { user ->
                val initials = resolveInitials(user.resolveDisplayName()) ?: FeedUiState().userInitials
                _uiState.update { it.copy(userInitials = initials) }
            }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // İçerik zaten varsa sessiz tazele: spinner gösterme, mevcut içeriği koru (flash yok).
            val hasContent = _uiState.value.run {
                recentlyPlayed.isNotEmpty() || forYou.isNotEmpty() || recommendations.isNotEmpty()
            }
            _uiState.update { it.copy(isLoading = !hasContent, errorMessage = null) }
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
                        isRefreshing = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Tazelemede içerik varsa koru (sessiz başarısızlık); yalnızca ilk yüklemede hata göster.
                _uiState.update {
                    if (hasContent) {
                        it.copy(isLoading = false, isRefreshing = false)
                    } else {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = e.toAppError().toUserMessage(ErrorContext.HOME),
                        )
                    }
                }
            }
        }
    }
}
