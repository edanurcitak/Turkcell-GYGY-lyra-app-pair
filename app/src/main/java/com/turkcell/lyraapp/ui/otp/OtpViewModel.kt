package com.turkcell.lyraapp.ui.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.OtpAuthRepository
import com.turkcell.lyraapp.ui.navigation.LyraDestinations
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

/**
 * OTP ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Doğrulanacak numara navigasyon argümanı olarak [SavedStateHandle]'dan okunur — `NavController`
 * ViewModel'e sızmaz (mevcut konvansiyon, bkz. [com.turkcell.lyraapp.ui.player.PlayerViewModel]).
 * "Doğrula" kodu [OtpAuthRepository.verifyOtp] ile doğrular; başarıda token saklanır ve
 * `firstTime`'a göre profil tamamlama veya ana ekrana yönlendiren tek seferlik olay yayınlanır.
 */
@HiltViewModel
class OtpViewModel @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val phone: String = checkNotNull(savedStateHandle[LyraDestinations.OTP_ARG_PHONE]) {
        "OtpViewModel için 'phone' nav argümanı zorunludur."
    }

    private val _uiState = MutableStateFlow(OtpUiState(phone = phone))
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<OtpEffect>(Channel.BUFFERED)
    val effect: Flow<OtpEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: OtpIntent) {
        when (intent) {
            is OtpIntent.CodeChanged ->
                _uiState.update {
                    it.copy(
                        code = intent.value.filter(Char::isDigit).take(OTP_CODE_LENGTH),
                        errorMessage = null,
                    )
                }

            OtpIntent.Verify -> verify()
            OtpIntent.Resend -> resend()
        }
    }

    private fun verify() {
        val state = _uiState.value
        if (state.isSubmitting || state.code.length < OTP_CODE_LENGTH) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            otpAuthRepository.verifyOtp(phone, state.code)
                .onSuccess { session ->
                    _uiState.update { it.copy(isSubmitting = false) }
                    val needsProfile = session.firstTime || session.user?.profileCompleted == false
                    _effect.send(
                        if (needsProfile) OtpEffect.NavigateToCompleteInfo else OtpEffect.NavigateToHome,
                    )
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.toAppError().toUserMessage(ErrorContext.OTP),
                        )
                    }
                }
        }
    }

    private fun resend() {
        if (_uiState.value.isResending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, code = "") }
            otpAuthRepository.requestOtp(phone)
            _uiState.update { it.copy(isResending = false) }
        }
    }
}
