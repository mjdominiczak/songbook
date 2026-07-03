package com.mjdominiczak.songbook.presentation

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.ObserveAllSongsUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import com.mjdominiczak.songbook.presentation.list.SongListViewModel
import kotlinx.coroutines.CompletableDeferred
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

    @Test
    fun init_withSavedSongs_receivesSongsFromObservationPath() = runTest(coroutineRule.dispatcher) {
        val savedSongs = listOf(song(id = 4, title = "Saved song"))

        val viewModel = songListViewModel(songs = savedSongs)

        assertThat(viewModel.state.value.songs).isEqualTo(savedSongs)
        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.isRefreshing).isFalse()
    }

    @Test
    fun init_withEmptyCacheAndRunningRefresh_showsInitialLoading() = runTest(coroutineRule.dispatcher) {
        val refreshGate = CompletableDeferred<RefreshAllSongsResult>()
        val viewModel = songListViewModel(
            songs = emptyList(),
            autoAdvance = false,
            refreshBlock = { refreshGate.await() },
        )

        advanceUntilIdle()

        assertThat(viewModel.state.value.songs).isEmpty()
        assertThat(viewModel.state.value.isInitialLoading).isTrue()
        assertThat(viewModel.state.value.isRefreshing).isTrue()
        assertThat(viewModel.state.value.blockingError).isNull()
        assertThat(viewModel.state.value.nonBlockingRefreshError).isNull()
    }

    @Test
    fun init_withCachedSongsAndRunningRefresh_keepsCachedSongsVisible() = runTest(coroutineRule.dispatcher) {
        val cachedSongs = listOf(song(id = 5, title = "Cached song"))
        val refreshGate = CompletableDeferred<RefreshAllSongsResult>()
        val viewModel = songListViewModel(
            songs = cachedSongs,
            autoAdvance = false,
            refreshBlock = { refreshGate.await() },
        )

        advanceUntilIdle()

        assertThat(viewModel.state.value.songs).isEqualTo(cachedSongs)
        assertThat(viewModel.state.value.isInitialLoading).isFalse()
        assertThat(viewModel.state.value.isRefreshing).isTrue()
        assertThat(viewModel.state.value.blockingError).isNull()
        assertThat(viewModel.state.value.nonBlockingRefreshError).isNull()
    }

    @Test
    fun init_withCachedSongsAndRefreshFailure_keepsSongsAndExposesNonBlockingError() =
        runTest(coroutineRule.dispatcher) {
            val cachedSongs = listOf(song(id = 6, title = "Cached song"))
            val repository = FakeSongRepository(
                observedSongs = cachedSongs,
                refreshBlock = { RefreshAllSongsResult.Failure(RefreshSongsError.NetworkUnavailable) },
            )

            val viewModel = songListViewModel(repository = repository)

            assertThat(repository.refreshCalls).isEqualTo(2)
            assertThat(viewModel.state.value.songs).isEqualTo(cachedSongs)
            assertThat(viewModel.state.value.isInitialLoading).isFalse()
            assertThat(viewModel.state.value.isRefreshing).isFalse()
            assertThat(viewModel.state.value.blockingError).isNull()
            assertThat(viewModel.state.value.nonBlockingRefreshError)
                .isEqualTo(RefreshSongsError.NetworkUnavailable)
        }

    @Test
    fun init_withEmptyCacheAndRefreshFailure_exposesBlockingError() = runTest(coroutineRule.dispatcher) {
        val repository = FakeSongRepository(
            observedSongs = emptyList(),
            refreshBlock = { RefreshAllSongsResult.Failure(RefreshSongsError.NetworkUnavailable) },
        )

        val viewModel = songListViewModel(repository = repository)

        assertThat(repository.refreshCalls).isEqualTo(2)
        assertThat(viewModel.state.value.songs).isEmpty()
        assertThat(viewModel.state.value.isInitialLoading).isFalse()
        assertThat(viewModel.state.value.isRefreshing).isFalse()
        assertThat(viewModel.state.value.blockingError)
            .isEqualTo(RefreshSongsError.NetworkUnavailable)
        assertThat(viewModel.state.value.nonBlockingRefreshError).isNull()
    }

    @Test
    fun onRefreshErrorShown_clearsNonBlockingRefreshError() = runTest(coroutineRule.dispatcher) {
        val viewModel = songListViewModel(
            songs = listOf(song(id = 7, title = "Cached song")),
            refreshBlock = { RefreshAllSongsResult.Failure(RefreshSongsError.NetworkUnavailable) },
        )

        viewModel.onRefreshErrorShown()

        assertThat(viewModel.state.value.nonBlockingRefreshError).isNull()
    }

    private fun TestScope.songListViewModel(
        songs: List<Song> = defaultSongs,
        autoAdvance: Boolean = true,
        refreshBlock: suspend () -> RefreshAllSongsResult = {
            RefreshAllSongsResult.Success(emptyList())
        },
    ): SongListViewModel {
        val repository = FakeSongRepository(
            observedSongs = songs,
            refreshBlock = refreshBlock,
        )
        return songListViewModel(repository, autoAdvance)
    }

    private fun TestScope.songListViewModel(
        repository: FakeSongRepository,
        autoAdvance: Boolean = true,
    ): SongListViewModel {
        return SongListViewModel(
            RefreshAllSongsUseCase(repository),
            ObserveAllSongsUseCase(repository),
        ).also {
            if (autoAdvance) {
                advanceUntilIdle()
            }
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
    observedSongs: List<Song>,
    private val refreshBlock: suspend () -> RefreshAllSongsResult,
) : SongRepository {
    private val observedSongs = MutableStateFlow(observedSongs)
    var refreshCalls = 0
        private set

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by SongListViewModelTest")

    override fun observeAllSongs(): Flow<List<Song>> = observedSongs

    override suspend fun getAllSongs(): List<Song> =
        error("getAllSongs is not used by SongListViewModelTest")

    override suspend fun refreshAllSongs(): RefreshAllSongsResult {
        refreshCalls++
        return refreshBlock()
    }

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by SongListViewModelTest")
}
