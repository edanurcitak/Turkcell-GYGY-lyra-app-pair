package com.turkcell.lyraapp.ui.createplaylist

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
 * Yeni çalma listesi ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * "Şarkı ekle" listesini mevcut [SongRepository] üzerinden Streaming API'den yükler
 * (FeedViewModel ile aynı yükleme/hata deseni; yeni repository eklenmez). Ad/açıklama,
 * "Herkese açık" anahtarı ve şarkı seçimi saf UI durumudur; reducer içinde işlenir.
 *
 * Not (§2.2 / §4.6): Kaydetme uç noktası olmadığından kaydetme aksiyonu/Effect yoktur.
 */
@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: CreatePlaylistIntent) {
        when (intent) {
            is CreatePlaylistIntent.NameChanged ->
                _uiState.update { it.copy(name = intent.value) }

            is CreatePlaylistIntent.DescriptionChanged ->
                _uiState.update { it.copy(description = intent.value) }

            CreatePlaylistIntent.TogglePublic ->
                _uiState.update { it.copy(isPublic = !it.isPublic) }

            is CreatePlaylistIntent.ToggleSongSelection -> _uiState.update {
                val next = if (intent.songId in it.selectedSongIds) {
                    it.selectedSongIds - intent.songId
                } else {
                    it.selectedSongIds + intent.songId
                }
                it.copy(selectedSongIds = next)
            }

            CreatePlaylistIntent.Refresh -> load()
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
