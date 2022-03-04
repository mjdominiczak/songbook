package com.mjdominiczak.songbook.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.GetAllSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase
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
            .sortedBy { it.title }

    init {
        getAllSongs()
    }

    fun getAllSongs() {
        getAllSongsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> setData(result.data)
                is Resource.Error -> setError(result.message)
                is Resource.Loading -> _state.value = SongListState(isLoading = true)
            }

        }.launchIn(viewModelScope)
    }

    private fun setData(songs: List<Song>?) {
        _state.value = _state.value.copy(
            isLoading = false,
            songs = songs ?: emptyList()
        )
    }

    private fun setError(message: String?) {
        _state.value = _state.value.copy(
            isLoading = false,
            error = message ?: "Unexpected error occured"
        )
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