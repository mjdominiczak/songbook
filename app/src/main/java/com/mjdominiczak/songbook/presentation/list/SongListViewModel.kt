package com.mjdominiczak.songbook.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.AddSongUseCase
import com.mjdominiczak.songbook.domain.ChordCollector
import com.mjdominiczak.songbook.domain.GetAllSongsUseCase
import com.mjdominiczak.songbook.json.SongsData
import com.mjdominiczak.songbook.resolvers.ResourcesResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.Collator
import java.util.Locale
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase,
    private val addSongUseCase: AddSongUseCase,
    private val resourcesResolver: ResourcesResolver,
    private val gson: Gson,
) : ViewModel() {

    private var retryCount = 0
    private val retryLimit = 1

    private val _state = mutableStateOf(SongListState())
    val state: State<SongListState> = _state
    val songsFiltered: List<Song>
        get() = _state.value.songs
            .filter {
                _state.value.searchQuery.isEmpty() ||
                        _state.value.searchQuery.isNotEmpty()
                        && it.title.lowercase().contains(_state.value.searchQuery.lowercase())
            }
            .sortedWith(compareBy(Collator.getInstance(Locale.getDefault())) { it.title })

    init {
        getAllSongs()
    }

    fun getAllSongs() {
        getAllSongsUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data.isNullOrEmpty() && retryCount < retryLimit) {
                        scheduleRetry()
                    } else {
                        setData(result.data)
                        result.data?.let { ChordCollector(it).parseChords() }
                    }
                }
                is Resource.Error -> setError(result.message)
                is Resource.Loading -> _state.value = SongListState(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun scheduleRetry() {
        retryCount++
        delay(1500)
        getAllSongs()
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

    fun onLoadSongsClicked() {
        viewModelScope.launch {
            val inputStream = resourcesResolver.getAsset("RRN_2022.json")
            val json = BufferedReader(InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining())
            val songs = gson.fromJson(json, SongsData::class.java)
            addSongUseCase(songs.data)
        }
    }
}