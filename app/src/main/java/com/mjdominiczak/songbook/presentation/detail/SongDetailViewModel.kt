package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.domain.GetSongUseCase
import com.mjdominiczak.songbook.resolvers.PreferencesResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getSongUseCase: GetSongUseCase,
    private val preferencesResolver: PreferencesResolver,
) : ViewModel() {

    private var _state by mutableStateOf(SongDetailState())
    val state: SongDetailState
        get() = _state

    var showSettings by mutableStateOf(false)
    var displayChords by mutableStateOf(true)
    var wrapLines by mutableStateOf(false)

    init {
        savedStateHandle.get<Int>("songId")?.let { id ->
            if (id != -1) getSong(id)
        }
        preferencesResolver.displayChords
            .onEach { displayChords = it }
            .launchIn(viewModelScope)
        preferencesResolver.wrapLines
            .onEach { wrapLines = it }
            .launchIn(viewModelScope)
    }

    fun onSettingsClicked() {
        showSettings = true
    }

    fun onSettingsDismissed() {
        showSettings = false
    }

    fun onDisplayChordsChanged(value: Boolean) {
        viewModelScope.launch {
            preferencesResolver.setDisplayChords(value)
        }
    }

    fun onWrapLinesChanged(value: Boolean) {
        viewModelScope.launch {
            preferencesResolver.setWrapLines(value)
        }
    }

    private fun getSong(id: Int) {
        getSongUseCase(id).onEach { result ->
            _state = when (result) {
                is Resource.Success -> {
                    SongDetailState(song = result.data)
                }
                is Resource.Error -> {
                    SongDetailState(error = result.message ?: "Unexpected error occured")
                }
                is Resource.Loading -> {
                    SongDetailState(isLoading = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}