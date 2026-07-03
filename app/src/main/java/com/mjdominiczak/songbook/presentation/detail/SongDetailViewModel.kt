package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.ObserveSongUseCase
import com.mjdominiczak.songbook.domain.RefreshSongResult
import com.mjdominiczak.songbook.domain.RefreshSongUseCase
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.resolvers.SongPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSongUseCase: ObserveSongUseCase,
    private val refreshSongUseCase: RefreshSongUseCase,
    private val preferencesResolver: SongPreferences,
) : ViewModel() {

    private var _state by mutableStateOf(SongDetailState())
    val state: SongDetailState
        get() = _state

    var showSettings by mutableStateOf(false)
    var displayChords by mutableStateOf(true)
    var wrapLines by mutableStateOf(false)

    init {
        savedStateHandle.get<Int>("songId")?.let { id ->
            if (id != -1) {
                observeSong(id)
                refreshSong(id)
            }
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

    private fun observeSong(id: Int) {
        observeSongUseCase(id).onEach(::setData).launchIn(viewModelScope)
    }

    private fun refreshSong(id: Int) {
        viewModelScope.launch {
            _state = _state.copy(
                isInitialLoading = _state.song == null,
                isRefreshing = true,
                blockingError = null,
            )

            when (val result = refreshSongUseCase(id)) {
                is RefreshSongResult.Success -> setRefreshFinished()
                is RefreshSongResult.Failure -> setRefreshError(result.error)
            }
        }
    }

    private fun setData(song: Song?) {
        _state = _state.copy(
            song = song,
            isInitialLoading = song == null && _state.isRefreshing,
            blockingError = if (song == null) _state.blockingError else null,
        )
    }

    private fun setRefreshFinished() {
        _state = _state.copy(
            isInitialLoading = _state.song == null,
            isRefreshing = false,
            blockingError = null,
        )
    }

    private fun setRefreshError(error: RefreshSongsError) {
        _state = if (_state.song == null) {
            _state.copy(
                isInitialLoading = false,
                isRefreshing = false,
                blockingError = error,
            )
        } else {
            _state.copy(
                isInitialLoading = false,
                isRefreshing = false,
                blockingError = null,
            )
        }
    }
}
