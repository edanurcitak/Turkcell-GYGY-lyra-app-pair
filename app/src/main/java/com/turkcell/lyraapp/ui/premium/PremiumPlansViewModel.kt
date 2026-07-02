package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.util.ErrorContext
import com.turkcell.lyraapp.util.toAppError
import com.turkcell.lyraapp.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Premium plan seçim ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Planları [MembershipRepository] üzerinden gerçek API'den yükler ([FeedViewModel] loading/error
 * deseni: tek [StateFlow], `try/catch`). Açılışta yükler; hata sonrası [PremiumPlansIntent.Retry]
 * ile tekrar dener. Plan seçimi ([PremiumPlansIntent.PlanSelected]) yalnızca UI durumunu günceller.
 *
 * Satın alma (`memberships/checkout`) kart formu gerektirir; bu ekranın kapsamı dışındadır (§4.6).
 */
@HiltViewModel
class PremiumPlansViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumPlansUiState())
    val uiState: StateFlow<PremiumPlansUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: PremiumPlansIntent) {
        when (intent) {
            is PremiumPlansIntent.PlanSelected ->
                _uiState.update { it.copy(selectedPlanId = intent.planId) }

            PremiumPlansIntent.Retry -> load()
            PremiumPlansIntent.PullRefresh -> load(isPull = true)
        }
    }

    /**
     * Planları yükler. [isPull] `true` ise (pull-to-refresh) tam-ekran spinner gösterilmez; mevcut
     * içerik görünür kalır, yenileme yalnızca üstteki dönen göstergeyle ([PremiumPlansUiState.isRefreshing])
     * belirtilir ve başarısız tazelemede içerik korunur (FeedViewModel deseni).
     */
    private fun load(isPull: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (isPull) it.isLoading else true,
                    isRefreshing = isPull,
                    errorMessage = null,
                )
            }
            try {
                val plans = membershipRepository.getPlans()
                // Tasarımda "Aylık" (recurring) plan seçili gelir; yoksa ilk plana düşülür.
                val defaultId = plans.firstOrNull { it.type == "recurring" }?.id
                    ?: plans.firstOrNull()?.id.orEmpty()
                _uiState.update {
                    // Pull-to-refresh'te kullanıcının (hâlâ geçerli olan) seçimini koru; ilk yüklemede varsayılana ayarla.
                    val keepSelection = isPull && plans.any { plan -> plan.id == it.selectedPlanId }
                    it.copy(
                        plans = plans,
                        selectedPlanId = if (keepSelection) it.selectedPlanId else defaultId,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Pull-to-refresh'te planlar zaten yüklüyse koru (sessiz başarısızlık); aksi halde hatayı göster.
                _uiState.update {
                    if (isPull && it.plans.isNotEmpty()) {
                        it.copy(isLoading = false, isRefreshing = false)
                    } else {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = e.toAppError().toUserMessage(ErrorContext.PREMIUM),
                        )
                    }
                }
            }
        }
    }
}
