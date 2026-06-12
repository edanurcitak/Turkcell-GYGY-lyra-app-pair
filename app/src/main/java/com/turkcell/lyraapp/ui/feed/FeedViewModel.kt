package com.turkcell.lyraapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.feed.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ana sayfa (feed) ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * [FeedRepository]'den (mock) ana sayfa içeriğini yükler ve tek bir [StateFlow] üzerinden
 * [FeedUiState] yayınlar. Bağımlılık Hilt tarafından constructor ile enjekte edilir.
 */
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
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
            _uiState.update { it.copy(isLoading = true) }
            val feed = feedRepository.getHomeFeed()
            _uiState.update {
                it.copy(
                    greeting = feed.greeting,
                    userInitials = feed.userInitials,
                    quickPicks = feed.quickPicks,
                    recentlyPlayed = feed.recentlyPlayed,
                    playlists = feed.playlists,
                    isLoading = false,
                )
            }
        }
    }
}
