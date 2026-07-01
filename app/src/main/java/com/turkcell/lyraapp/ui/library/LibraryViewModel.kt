package com.turkcell.lyraapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.connectivity.ConnectivityObserver
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
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
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeConnectivity()
        observeFavorites()
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
        }
    }

    /** Bağlantı durumu değiştikçe kütüphaneyi yeniden yükler (çevrimdışı → yalnızca indirilenler). */
    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isOnline.collect { online -> load(online) }
        }
    }

    /**
     * Beğenilen şarkı kümesi ([FavoritesRepository.likedIds]) değiştikçe "Beğenilen Şarkılar"
     * listesinin şarkı sayısını **anında** günceller — böylece bir şarkı Player'da beğenilince
     * Kütüphane'deki sayı elle yenileme (pull-to-refresh) gerekmeden canlı değişir.
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.likedIds.collect { liked ->
                _uiState.update { state ->
                    state.copy(
                        playlists = state.playlists.map { playlist ->
                            if (playlist.isLiked) playlist.copy(songCount = liked.size) else playlist
                        },
                    )
                }
            }
        }
    }

    /**
     * Çalma listelerini yükler (ekrana giriş ve bağlantı değişiminde). Çevrimdışıyken ağ uçlarına
     * dokunmadan yalnızca "İndirilen Şarkılar" gösterilir. "Beğenilen Şarkılar" sayısı ayrıca
     * [observeFavorites] ile canlı tutulur (pull-to-refresh kaldırıldı — dinamik yapı).
     */
    private fun load(online: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, isOffline = !online)
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
                        isOffline = false,
                    )
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
