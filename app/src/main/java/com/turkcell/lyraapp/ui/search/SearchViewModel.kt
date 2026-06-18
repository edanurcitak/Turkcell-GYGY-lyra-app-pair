package com.turkcell.lyraapp.ui.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Ara ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [SearchUiState]'i tek bir [StateFlow] üzerinden yayınlar; gelen [SearchIntent]'leri
 * [onIntent] içinde reducer mantığıyla işler. Arama şimdilik yalnızca metni state'te tutar;
 * arama çalıştırma / repository katmanı talep edilmediğinden eklenmez (§4.6).
 */
@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged ->
                _uiState.update { it.copy(query = intent.value) }
        }
    }
}
