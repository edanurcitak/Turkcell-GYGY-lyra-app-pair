package com.turkcell.lyraapp.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.auth.OtpAuthRepository
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
 * Register ("Bilgilerini tamamla") ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Tek bir [StateFlow] üzerinden [RegisterUiState] yayınlar; gelen [RegisterIntent]'leri [onIntent]
 * içinde reducer mantığıyla işler. "Tamamla"da ad/soyad/doğum tarihini [OtpAuthRepository.updateInformations]
 * ile (saklı erişim token'ıyla) kaydeder ve başarıda tek seferlik [RegisterEffect.NavigateToHome] yayınlar.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val otpAuthRepository: OtpAuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RegisterEffect>(Channel.BUFFERED)
    val effect: Flow<RegisterEffect> = _effect.receiveAsFlow()

    fun onIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.FirstNameChanged ->
                _uiState.update { it.copy(firstName = intent.value, errorMessage = null) }

            is RegisterIntent.LastNameChanged ->
                _uiState.update { it.copy(lastName = intent.value, errorMessage = null) }

            is RegisterIntent.BirthDayChanged ->
                _uiState.update {
                    it.copy(birthDay = intent.value.filter(Char::isDigit).take(2), errorMessage = null)
                }

            is RegisterIntent.BirthMonthChanged ->
                _uiState.update {
                    it.copy(birthMonth = intent.value.filter(Char::isDigit).take(2), errorMessage = null)
                }

            is RegisterIntent.BirthYearChanged ->
                _uiState.update {
                    it.copy(birthYear = intent.value.filter(Char::isDigit).take(4), errorMessage = null)
                }

            RegisterIntent.SubmitClicked -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return
        val birthDate = formatBirthDate(state.birthYear, state.birthMonth, state.birthDay)
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            otpAuthRepository.updateInformations(
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                birthDate = birthDate,
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(RegisterEffect.NavigateToHome)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.toAppError().toUserMessage(ErrorContext.REGISTER),
                    )
                }
            }
        }
    }

    /** Gün/ay/yıl alanlarını API'nin beklediği `YYYY-MM-DD` biçimine birleştirir. */
    private fun formatBirthDate(year: String, month: String, day: String): String =
        "%04d-%02d-%02d".format(
            year.toIntOrNull() ?: 0,
            month.toIntOrNull() ?: 0,
            day.toIntOrNull() ?: 0,
        )
}
