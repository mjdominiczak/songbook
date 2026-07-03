package com.mjdominiczak.songbook.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.ObserveAllSongsUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.presentation.components.TagParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val refreshAllSongsUseCase: RefreshAllSongsUseCase,
    observeAllSongsUseCase: ObserveAllSongsUseCase,
) : ViewModel() {

    private val _state = mutableStateOf(SongListState())
    val state: State<SongListState> = _state
    val songsFiltered: List<Song>
        get() = _state.value.songs
            .filter {
                _state.value.searchQuery.isEmpty() ||
                    _state.value.searchQuery.isNotEmpty()
                    && it.title.lowercase().contains(_state.value.searchQuery.lowercase())
            }
            .filter {
                _state.value.tagsFilter.isEmpty() ||
                    it.tags.containsAll(_state.value.tagsFilter)
            }
            .sortedWith(compareBy(Collator.getInstance(Locale.getDefault())) { it.title })

    val availableTags: List<TagParams>
        get() = _state.value.songs
            .asSequence()
            .flatMap { it.tags }
            .distinct()
            .filterNot { it == "RRN 2022" } // Exclude Songbook name for now
            .sorted()
            .map {
                TagParams(
                    name = it,
                    selected = it in _state.value.tagsFilter,
                    onClick = { onTagClicked(it) }
                )
            }
            .toList()

    private fun onTagClicked(tag: String) {
        val tagsSet = _state.value.tagsFilter.toMutableSet().apply {
            if (!add(tag)) remove(tag)
        }.toSet()
        _state.value = _state.value.copy(tagsFilter = tagsSet)
    }

    init {
        observeAllSongsUseCase().onEach(::setData).launchIn(viewModelScope)
        getAllSongs()
    }

    fun getAllSongs() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isInitialLoading = _state.value.songs.isEmpty(),
                isRefreshing = true,
                blockingError = null,
                nonBlockingRefreshError = null,
            )

            when (val result = refreshAllSongsUseCase()) {
                is RefreshAllSongsResult.Success -> setRefreshFinished()
                is RefreshAllSongsResult.Failure -> setRefreshError(result.error)
            }
        }
    }

    private fun setData(songs: List<Song>?) {
        val savedSongs = songs ?: emptyList()
        _state.value = _state.value.copy(
            songs = savedSongs,
            isInitialLoading = savedSongs.isEmpty() && _state.value.isRefreshing,
            blockingError = if (savedSongs.isEmpty()) _state.value.blockingError else null,
        )
    }

    private fun setRefreshFinished() {
        _state.value = _state.value.copy(
            isInitialLoading = false,
            isRefreshing = false,
            blockingError = null,
            nonBlockingRefreshError = null,
        )
    }

    private fun setRefreshError(error: RefreshSongsError) {
        _state.value = if (_state.value.songs.isEmpty()) {
            _state.value.copy(
                isInitialLoading = false,
                isRefreshing = false,
                blockingError = error,
                nonBlockingRefreshError = null,
            )
        } else {
            _state.value.copy(
                isInitialLoading = false,
                isRefreshing = false,
                blockingError = null,
                nonBlockingRefreshError = error,
            )
        }
    }

    fun onRefreshErrorShown() {
        _state.value = _state.value.copy(nonBlockingRefreshError = null)
    }

    fun activateSearch() {
        _state.value = _state.value.copy(isSearchActive = true)
    }

    fun deactivateSearch() {
        _state.value = _state.value.copy(isSearchActive = false, searchQuery = "")
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}
