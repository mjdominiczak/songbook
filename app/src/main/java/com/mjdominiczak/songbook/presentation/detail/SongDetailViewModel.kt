package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.domain.GetSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSongUseCase: GetSongUseCase
) : ViewModel() {

    private val _state = mutableStateOf(SongDetailState())
    val state: State<SongDetailState>
        get() = _state

    init {
        savedStateHandle.get<Int>("songId")?.let { id ->
            if (id != -1) getSong(id)
        }
    }

    private fun getSong(id: Int) {
        getSongUseCase(id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = SongDetailState(song = result.data)
                }
                is Resource.Error -> {
                    _state.value =
                        SongDetailState(error = result.message ?: "Unexpected error occured")
                }
                is Resource.Loading -> {
                    _state.value = SongDetailState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}