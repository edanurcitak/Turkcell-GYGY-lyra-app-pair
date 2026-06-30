package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.connectivity.ConnectivityObserver
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
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeConnectivity()
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

            LibraryIntent.Refresh -> load(connectivityObserver.currentlyOnline())
            LibraryIntent.PullRefresh ->
                load(connectivityObserver.currentlyOnline(), isPull = true)
        }
    }

    /** Bağlantı durumu değiştikçe kütüphaneyi yeniden yükler (çevrimdışı → yalnızca indirilenler). */
    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online -> load(online) }
        }
    }

    /**
     * Çalma listelerini yükler. [isPull] `true` ise (pull-to-refresh) tam-ekran spinner gösterilmez;
     * mevcut liste görünür kalır, yenileme yalnızca üstteki dönen göstergeyle ([LibraryUiState.isRefreshing])
     * belirtilir ve başarısız tazelemede içerik korunur (FeedViewModel deseni).
     */
    private fun load(online: Boolean, isPull: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = if (isPull) it.isLoading else true,
                    isRefreshing = isPull,
                    errorMessage = null,
                    isOffline = !online,
                )
            }
            // Çevrimdışı: ağ uçlarına hiç dokunmadan yalnızca "İndirilen Şarkılar" (varsa).
            if (!online) {
                val downloaded = playlistRepository.getDownloadedPlaylist()
                // Görünür indirme yoksa (free ya da boş) liste boş kalır → EmptyState gösterilir.
                val offlinePlaylists = if (downloaded.songCount > 0) listOf(downloaded) else emptyList()
                _uiState.update {
                    it.copy(
                        playlists = offlinePlaylists,
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = true,
                    )
                }
                return@launch
            }
            try {
                val playlists = playlistRepository.getPlaylists()
                _uiState.update {
                    it.copy(
                        playlists = playlists.sortedFor(it.sortOrder),
                        isLoading = false,
                        isRefreshing = false,
                        isOffline = false,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Pull-to-refresh'te liste varsa koru (sessiz başarısızlık); aksi halde hatayı göster.
                _uiState.update {
                    if (isPull && it.playlists.isNotEmpty()) {
                        it.copy(isLoading = false, isRefreshing = false)
                    } else {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "Çalma listeleri yüklenemedi. Lütfen tekrar deneyin.",
                        )
                    }
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
