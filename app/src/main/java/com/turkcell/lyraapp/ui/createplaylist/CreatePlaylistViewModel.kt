package com.turkcell.lyraapp.ui.createplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.lyraapp.data.feed.SongRepository
import com.turkcell.lyraapp.data.playlist.PlaylistRepository
import com.turkcell.lyraapp.util.ErrorContext
import com.turkcell.lyraapp.util.toAppError
import com.turkcell.lyraapp.util.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Yeni çalma listesi ekranının MVI ViewModel'i (AGENTS.MD §4.4).
 *
 * "Şarkı ekle" listesini mevcut [SongRepository] üzerinden Streaming API'den yükler
 * (FeedViewModel ile aynı yükleme/hata deseni). Ad/açıklama, "Herkese açık" anahtarı ve şarkı
 * seçimi saf UI durumudur; reducer içinde işlenir.
 *
 * "Kaydet"te liste [PlaylistRepository.createPlaylist] ile oluşturulur, seçili şarkılar
 * [PlaylistRepository.addSong] ile eklenir; başarıda tek seferlik [CreatePlaylistEffect.NavigateBack]
 * yayınlanır (Register deseni).
 */
@HiltViewModel
class CreatePlaylistViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _effect = Channel<CreatePlaylistEffect>(Channel.BUFFERED)
    val effect: Flow<CreatePlaylistEffect> = _effect.receiveAsFlow()

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

            CreatePlaylistIntent.SaveClicked -> save()

            CreatePlaylistIntent.Refresh -> load()
        }
    }

    /**
     * Listeyi oluşturur ve seçili şarkıları ekler. Ad boşsa ya da halihazırda kaydediliyorsa yok sayar.
     *
     * Oluşturma başarısızsa hata gösterilip ekranda kalınır. Şarkı eklemeleri **en iyi çaba** ile
     * paralel yapılır (tekil ekleme hatası, oluşturulmuş listeyi düşürmez); oluşturma başarılıysa
     * geri dönülür.
     */
    private fun save() {
        val state = _uiState.value
        if (state.isSaving || state.name.isBlank()) return
        val selected = state.selectedSongIds.toList()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val playlist = playlistRepository.createPlaylist(
                    name = state.name.trim(),
                    description = state.description.trim().takeIf { it.isNotBlank() },
                )
                coroutineScope {
                    selected.map { songId ->
                        async { runCatching { playlistRepository.addSong(playlist.id, songId) } }
                    }.awaitAll()
                }
                _uiState.update { it.copy(isSaving = false) }
                _effect.send(CreatePlaylistEffect.NavigateBack)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.toAppError().toUserMessage(ErrorContext.CREATE_PLAYLIST),
                    )
                }
            }
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
                        errorMessage = e.toAppError().toUserMessage(ErrorContext.CREATE_PLAYLIST),
                    )
                }
            }
        }
    }
}
