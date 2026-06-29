package com.turkcell.lyraapp.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.MembershipRepository
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
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val plans = membershipRepository.getPlans()
                // Tasarımda "Aylık" (recurring) plan seçili gelir; yoksa ilk plana düşülür.
                val defaultId = plans.firstOrNull { it.type == "recurring" }?.id
                    ?: plans.firstOrNull()?.id.orEmpty()
                _uiState.update {
                    it.copy(plans = plans, selectedPlanId = defaultId, isLoading = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Planlar yüklenemedi. Lütfen tekrar deneyin.")
                }
            }
        }
    }
}
