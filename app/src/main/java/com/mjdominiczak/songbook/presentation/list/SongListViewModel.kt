package com.mjdominiczak.songbook.presentation.list

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.ObserveAllSongsUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsUseCase
import com.mjdominiczak.songbook.presentation.components.TagParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import retrofit2.HttpException
import java.text.Collator
import java.util.Locale
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val refreshAllSongsUseCase: RefreshAllSongsUseCase,
    observeAllSongsUseCase: ObserveAllSongsUseCase,
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
                isLoading = true,
                error = null,
            )
            try {
                val refreshedSongs = refreshAllSongsUseCase()
                if (refreshedSongs.isEmpty() && _state.value.songs.isEmpty() && retryCount < retryLimit) {
                    scheduleRetry()
                } else {
                    setRefreshFinished()
                }
            } catch (e: HttpException) {
                setError(e.localizedMessage)
            } catch (e: IOException) {
                setError(e.localizedMessage)
            }
        }
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

    private fun setRefreshFinished() {
        _state.value = _state.value.copy(isLoading = false)
    }

    private fun setError(message: String?) {
        _state.value = if (_state.value.songs.isEmpty()) {
            _state.value.copy(
                isLoading = false,
                error = message ?: "Unexpected error occured"
            )
        } else {
            _state.value.copy(isLoading = false)
        }
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
