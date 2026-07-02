package com.turkcell.lyraapp.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.membership.CardInput
import com.turkcell.lyraapp.data.membership.MembershipRepository
import com.turkcell.lyraapp.data.membership.PaymentDeclinedException
import com.turkcell.lyraapp.ui.navigation.LyraDestinations
import com.turkcell.lyraapp.util.AppError
import com.turkcell.lyraapp.util.ErrorContext
import com.turkcell.lyraapp.util.toAppError
import com.turkcell.lyraapp.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Ödeme (checkout) ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Satın alınacak plan, nav argümanı `planId` ([SavedStateHandle]) üzerinden gerçek API'den çözülür
 * ([MembershipRepository.getPlans]) — `NavController` ViewModel'e sızmaz ([PlayerViewModel]/[OtpViewModel]
 * deseni). Kart alanları girişte rakama indirgenir/sınırlandırılır ([OtpViewModel] kod filtresi deseni).
 *
 * "Öde" ([PaymentIntent.PayClicked]) [MembershipRepository.checkout] çağırır; başarıda tier zaten
 * repository'de premium'a çevrilir ve [PaymentEffect.PaymentApproved] yayınlanır (profile dönüş).
 * Kart reddi ([PaymentDeclinedException]) diğer hatalardan ayrı, kullanıcıya özel mesaj gösterir.
 */
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val membershipRepository: MembershipRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val planId: String = checkNotNull(savedStateHandle[LyraDestinations.PAYMENT_ARG_PLAN_ID]) {
        "PaymentViewModel için 'planId' nav argümanı zorunludur."
    }

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _effect = Channel<PaymentEffect>(Channel.BUFFERED)
    val effect: Flow<PaymentEffect> = _effect.receiveAsFlow()

    init {
        loadPlan()
    }

    fun onIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.CardNumberChanged ->
                _uiState.update {
                    it.copy(cardNumber = intent.value.filter(Char::isDigit).take(16), errorMessage = null)
                }

            is PaymentIntent.HolderNameChanged ->
                _uiState.update { it.copy(holderName = intent.value.take(40), errorMessage = null) }

            is PaymentIntent.ExpiryChanged ->
                _uiState.update {
                    it.copy(expiry = intent.value.filter(Char::isDigit).take(4), errorMessage = null)
                }

            is PaymentIntent.CvcChanged ->
                _uiState.update {
                    it.copy(cvc = intent.value.filter(Char::isDigit).take(4), errorMessage = null)
                }

            PaymentIntent.PayClicked -> pay()
            PaymentIntent.Retry -> loadPlan()
        }
    }

    private fun loadPlan() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val plan = membershipRepository.getPlans().firstOrNull { it.id == planId }
                _uiState.update {
                    if (plan == null) {
                        it.copy(isLoading = false, errorMessage = "Plan bulunamadı. Geri dönüp tekrar deneyin.")
                    } else {
                        it.copy(plan = plan, isLoading = false)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.toAppError().toUserMessage(ErrorContext.PAYMENT))
                }
            }
        }
    }

    private fun pay() {
        val state = _uiState.value
        val plan = state.plan ?: return
        if (!state.canPay) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            membershipRepository.checkout(
                plan = plan.type,
                card = CardInput(
                    // Doc örneğiyle aynı biçim ("4242 4242 4242 4242"); sunucu kart no'ya göre karar verir.
                    number = state.cardNumber.chunked(4).joinToString(" "),
                    expMonth = state.expiry.take(2).toInt(),
                    expYear = 2000 + state.expiry.takeLast(2).toInt(),
                    cvc = state.cvc,
                    holderName = state.holderName.trim(),
                ),
            ).onSuccess {
                _uiState.update { it.copy(isProcessing = false) }
                _effect.send(PaymentEffect.PaymentApproved)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        // Repo 402'yi PaymentDeclinedException'a çevirdiğinden toAppError onu göremez;
                        // 402 mesajını merkezi tablodan almak için AppError.Api(402) açıkça kullanılır.
                        errorMessage = if (e is PaymentDeclinedException) {
                            AppError.Api(code = 402).toUserMessage(ErrorContext.PAYMENT)
                        } else {
                            e.toAppError().toUserMessage(ErrorContext.PAYMENT)
                        },
                    )
                }
            }
        }
    }
}
