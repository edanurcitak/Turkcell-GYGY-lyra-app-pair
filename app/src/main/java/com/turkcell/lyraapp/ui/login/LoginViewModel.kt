package com.turkcell.lyraapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.OtpAuthRepository
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
 * Login ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [LoginUiState]'i tek bir [StateFlow] üzerinden yayınlar; gelen [LoginIntent]'leri [onIntent]
 * içinde reducer mantığıyla işler. "Devam et"te telefon için OTP ister ([OtpAuthRepository.requestOtp])
 * ve başarılı olursa tek seferlik [LoginEffect.NavigateToOtp] olayını yayınlar.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect: Flow<LoginEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.PhoneNumberChanged ->
                _uiState.update { it.copy(phoneNumber = intent.value, errorMessage = null) }

            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val phone = state.phoneNumber.toE164()
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            otpAuthRepository.requestOtp(phone)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false) }
                    _effect.send(LoginEffect.NavigateToOtp(phone))
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = "Kod gönderilemedi. Numaranı kontrol edip tekrar dene.",
                        )
                    }
                }
        }
    }

    /**
     * Girilen ulusal numarayı API'nin beklediği E.164-ish ("+90…") biçimine çevirir:
     * yalnızca rakamlar tutulur, baştaki olası "0" atılır, başına "+90" eklenir.
     */
    private fun String.toE164(): String {
        val digits = filter(Char::isDigit).trimStart('0')
        return "+90$digits"
    }
}
