package com.turkcell.lyraapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.feed.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
 * Şarkı listesini [SongRepository] üzerinden Streaming API'den yükler ve tek bir [StateFlow]
 * ile [FeedUiState] yayınlar. Bağımlılık Hilt tarafından constructor ile enjekte edilir.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val songRepository: SongRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: FeedIntent) {
        when (intent) {
            FeedIntent.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val songs = songRepository.getSongs()
                _uiState.update { it.copy(songs = songs, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Şarkılar yüklenemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }
}
