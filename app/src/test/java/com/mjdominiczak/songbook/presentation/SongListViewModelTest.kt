package com.mjdominiczak.songbook.presentation

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.GetAllSongsUseCase
import com.mjdominiczak.songbook.domain.SongRepository
import com.mjdominiczak.songbook.presentation.list.SongListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongListViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun activateSearch_stateUpdatedCorrectly() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()

        viewModel.activateSearch()

        assertThat(viewModel.state.value.isSearchActive).isTrue()
    }

    @Test
    fun deactivateSearch_stateUpdatedCorrectly() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()
        viewModel.activateSearch()

        viewModel.deactivateSearch()

        assertThat(viewModel.state.value.isSearchActive).isFalse()
        assertThat(viewModel.state.value.searchQuery).isEmpty()
    }

    @Test
    fun onSearchQueryChanged_stateUpdatedCorrectly() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()

        viewModel.onSearchQueryChanged("test")

        assertThat(viewModel.state.value.searchQuery).isEqualTo("test")
    }

    @Test
    fun songsFiltered_withEmptyQuery_returnsFullList() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()

        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("")

        assertThat(viewModel.songsFiltered).hasSize(3)
    }

    @Test
    fun songsFiltered_withQueryMatchingMany_returnsAllMatches() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()

        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("ador")

        assertThat(viewModel.songsFiltered).hasSize(2)
    }

    @Test
    fun songsFiltered_withQueryMatchingNothing_returnsEmptyList() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel()

        viewModel.activateSearch()
        viewModel.onSearchQueryChanged("eerfgdzfgd")

        assertThat(viewModel.songsFiltered).isEmpty()
    }

    private fun TestScope.songListViewModel(
        songs: List<Song> = defaultSongs,
    ): SongListViewModel {
        val repository = FakeSongRepository(songs)
        return SongListViewModel(GetAllSongsUseCase(repository)).also {
            advanceUntilIdle()
        }
    }

    private companion object {
        val defaultSongs = listOf(
            song(id = 1, title = "Adoracja poranna"),
            song(id = 2, title = "Alleluja"),
            song(id = 3, title = "Wieczorna adoracja"),
        )

        fun song(id: Int, title: String) = Song(
            id = id,
            version = 1,
            title = title,
            tags = listOf("RRN 2022"),
        )
    }
}

private class FakeSongRepository(
    private val songs: List<Song>,
) : SongRepository {
    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by SongListViewModelTest")

    override suspend fun getAllSongs(): List<Song> = songs

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by SongListViewModelTest")
}
