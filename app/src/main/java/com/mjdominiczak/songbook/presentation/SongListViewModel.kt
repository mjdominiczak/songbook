package com.mjdominiczak.songbook.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.common.Resource
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

    init {
        getAllSongs()
    }

    fun getAllSongs() {
        getAllSongsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value =
                        SongListState(songs = result.data?.sortedBy { it.title } ?: emptyList())
                }
                is Resource.Error -> {
                    _state.value = SongListState(
                        error = result.message ?: "Unexpected error occured"
                    )
                }
                is Resource.Loading -> {
                    _state.value = SongListState(isLoading = true)
                }
            }

        }.launchIn(viewModelScope)
    }
}