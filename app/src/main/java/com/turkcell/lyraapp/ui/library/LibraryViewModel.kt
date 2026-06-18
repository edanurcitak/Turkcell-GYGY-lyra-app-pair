package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.playlist.Playlist
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Kütüphane ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * Çalma listelerini [PlaylistRepository] üzerinden Streaming API'den yükler ve tek bir
 * [StateFlow] ile [LibraryUiState] yayınlar (FeedViewModel ile aynı yükleme/hata deseni).
 * Görünüm modu ve sıralama saf UI durumudur; reducer içinde işlenir.
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onIntent(intent: LibraryIntent) {
        when (intent) {
            LibraryIntent.ToggleViewMode -> _uiState.update {
                it.copy(
                    viewMode = if (it.viewMode == LibraryViewMode.LIST) {
                        LibraryViewMode.GRID
                    } else {
                        LibraryViewMode.LIST
                    },
                )
            }

            LibraryIntent.ToggleSort -> _uiState.update {
                val next = if (it.sortOrder == LibrarySortOrder.RECENT) {
                    LibrarySortOrder.ALPHABETICAL
                } else {
                    LibrarySortOrder.RECENT
                }
                it.copy(sortOrder = next, playlists = it.playlists.sortedFor(next))
            }

            LibraryIntent.Refresh -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val playlists = playlistRepository.getPlaylists()
                _uiState.update {
                    it.copy(playlists = playlists.sortedFor(it.sortOrder), isLoading = false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Çalma listeleri yüklenemedi. Lütfen tekrar deneyin.",
                    )
                }
            }
        }
    }
}

/**
 * Sıralama düzenine göre listeyi sıralar; sabitlenmiş listeler ([Playlist.isPinned]) daima
 * en üstte kalır ("Beğenilen Şarkılar"). RECENT eklenme tarihine ([Playlist.createdAt]) göre
 * azalan, ALPHABETICAL ada göre artan sıralar.
 */
private fun List<Playlist>.sortedFor(order: LibrarySortOrder): List<Playlist> {
    val (pinned, rest) = partition { it.isPinned }
    val sortedRest = when (order) {
        LibrarySortOrder.RECENT -> rest.sortedByDescending { it.createdAt ?: "" }
        LibrarySortOrder.ALPHABETICAL -> rest.sortedBy { it.name.lowercase() }
    }
    return pinned + sortedRest
}
