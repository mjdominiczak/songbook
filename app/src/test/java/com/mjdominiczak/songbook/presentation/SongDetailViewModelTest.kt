package com.mjdominiczak.songbook.presentation

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.ObserveSongUseCase
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
import com.mjdominiczak.songbook.domain.RefreshSongResult
import com.mjdominiczak.songbook.domain.RefreshSongUseCase
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.domain.SongRepository
import com.mjdominiczak.songbook.presentation.detail.SongDetailViewModel
import com.mjdominiczak.songbook.resolvers.SongPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SongDetailViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun init_withCachedSong_receivesSongFromObservationPath() = runTest(coroutineRule.dispatcher) {
        val cachedSong = song(id = 1, title = "Cached")

        val viewModel = songDetailViewModel(savedSong = cachedSong)

        assertThat(viewModel.state.song).isEqualTo(cachedSong)
        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.isRefreshing).isFalse()
        assertThat(viewModel.state.blockingError).isNull()
    }

    @Test
    fun init_withCachedSongAndRunningRefresh_keepsCachedSongVisible() =
        runTest(coroutineRule.dispatcher) {
            val cachedSong = song(id = 2, title = "Cached")
            val refreshGate = CompletableDeferred<RefreshSongResult>()
            val viewModel = songDetailViewModel(
                savedSong = cachedSong,
                autoAdvance = false,
                refreshBlock = { refreshGate.await() },
            )

            advanceUntilIdle()

            assertThat(viewModel.state.song).isEqualTo(cachedSong)
            assertThat(viewModel.state.isLoading).isFalse()
            assertThat(viewModel.state.isRefreshing).isTrue()
            assertThat(viewModel.state.blockingError).isNull()
        }

    @Test
    fun init_withSuccessfulRefresh_updatesFromObservationPath() = runTest(coroutineRule.dispatcher) {
        val cachedSong = song(id = 3, title = "Cached")
        val remoteSong = song(id = 3, title = "Remote")
        val repository = FakeDetailSongRepository(
            savedSong = cachedSong,
            refreshedSong = remoteSong,
            refreshBlock = { RefreshSongResult.Success },
        )

        val viewModel = songDetailViewModel(repository = repository)

        assertThat(repository.refreshCalls).isEqualTo(1)
        assertThat(viewModel.state.song).isEqualTo(remoteSong)
        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.isRefreshing).isFalse()
        assertThat(viewModel.state.blockingError).isNull()
    }

    @Test
    fun init_withMissingCachedSongAndSuccessfulRefresh_showsObservedSong() =
        runTest(coroutineRule.dispatcher) {
            val remoteSong = song(id = 5, title = "Remote")
            val repository = FakeDetailSongRepository(
                savedSong = null,
                refreshedSong = remoteSong,
                refreshBlock = { RefreshSongResult.Success },
            )

            val viewModel = songDetailViewModel(repository = repository)

            assertThat(repository.refreshCalls).isEqualTo(1)
            assertThat(viewModel.state.song).isEqualTo(remoteSong)
            assertThat(viewModel.state.isLoading).isFalse()
            assertThat(viewModel.state.isRefreshing).isFalse()
            assertThat(viewModel.state.blockingError).isNull()
        }

    @Test
    fun init_withMissingCachedSongAndSuccessfulRefreshBeforeObservation_keepsInitialLoading() =
        runTest(coroutineRule.dispatcher) {
            val remoteSong = song(id = 6, title = "Remote")
            val repository = FakeDetailSongRepository(
                savedSong = null,
                refreshedSong = remoteSong,
                persistRefreshSuccess = false,
                refreshBlock = { RefreshSongResult.Success },
            )

            val viewModel = songDetailViewModel(repository = repository)

            assertThat(repository.refreshCalls).isEqualTo(1)
            assertThat(viewModel.state.song).isNull()
            assertThat(viewModel.state.isLoading).isTrue()
            assertThat(viewModel.state.isRefreshing).isFalse()
            assertThat(viewModel.state.blockingError).isNull()
        }

    @Test
    fun init_withCachedSongAndRefreshFailure_keepsCachedSongVisible() =
        runTest(coroutineRule.dispatcher) {
            val cachedSong = song(id = 4, title = "Cached")
            val repository = FakeDetailSongRepository(
                savedSong = cachedSong,
                refreshBlock = { RefreshSongResult.Failure(RefreshSongsError.NetworkUnavailable) },
            )

            val viewModel = songDetailViewModel(repository = repository)

            assertThat(repository.refreshCalls).isEqualTo(2)
            assertThat(viewModel.state.song).isEqualTo(cachedSong)
            assertThat(viewModel.state.isLoading).isFalse()
            assertThat(viewModel.state.isRefreshing).isFalse()
            assertThat(viewModel.state.blockingError).isNull()
        }

    @Test
    fun init_withMissingCachedSongAndRefreshFailure_exposesBlockingError() =
        runTest(coroutineRule.dispatcher) {
            val repository = FakeDetailSongRepository(
                savedSong = null,
                refreshBlock = { RefreshSongResult.Failure(RefreshSongsError.NetworkUnavailable) },
            )

            val viewModel = songDetailViewModel(repository = repository)

            assertThat(repository.refreshCalls).isEqualTo(2)
            assertThat(viewModel.state.song).isNull()
            assertThat(viewModel.state.isLoading).isFalse()
            assertThat(viewModel.state.isRefreshing).isFalse()
            assertThat(viewModel.state.blockingError)
                .isEqualTo(RefreshSongsError.NetworkUnavailable)
        }

    private fun TestScope.songDetailViewModel(
        savedSong: Song?,
        autoAdvance: Boolean = true,
        refreshBlock: suspend () -> RefreshSongResult = {
            RefreshSongResult.Success
        },
    ): SongDetailViewModel {
        val repository = FakeDetailSongRepository(
            savedSong = savedSong,
            refreshedSong = savedSong ?: song(id = 1, title = "Remote"),
            refreshBlock = refreshBlock,
        )
        return songDetailViewModel(repository, autoAdvance)
    }

    private fun TestScope.songDetailViewModel(
        repository: FakeDetailSongRepository,
        autoAdvance: Boolean = true,
    ): SongDetailViewModel {
        return SongDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("songId" to 1)),
            observeSongUseCase = ObserveSongUseCase(repository),
            refreshSongUseCase = RefreshSongUseCase(repository),
            preferencesResolver = FakeSongPreferences(),
        ).also {
            if (autoAdvance) {
                advanceUntilIdle()
            }
        }
    }

    private companion object {
        fun song(id: Int, title: String) = Song(
            id = id,
            version = 1,
            title = title,
            tags = listOf("RRN 2022"),
        )
    }
}

private class FakeDetailSongRepository(
    savedSong: Song?,
    private val refreshedSong: Song? = savedSong,
    private val persistRefreshSuccess: Boolean = true,
    private val refreshBlock: suspend () -> RefreshSongResult,
) : SongRepository {
    private val observedSong = MutableStateFlow(savedSong)
    var refreshCalls = 0
        private set

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by SongDetailViewModelTest")

    override fun observeAllSongs(): Flow<List<Song>> =
        error("observeAllSongs is not used by SongDetailViewModelTest")

    override fun observeSongById(id: Int): Flow<Song?> = observedSong

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        error("refreshAllSongs is not used by SongDetailViewModelTest")

    override suspend fun refreshSongById(id: Int): RefreshSongResult {
        refreshCalls++
        val result = refreshBlock()
        if (persistRefreshSuccess && result is RefreshSongResult.Success) {
            observedSong.value = refreshedSong
        }
        return result
    }
}

private class FakeSongPreferences : SongPreferences {
    override val displayChords = MutableStateFlow(true)
    override val wrapLines = MutableStateFlow(false)

    override suspend fun setDisplayChords(value: Boolean) {
        displayChords.value = value
    }

    override suspend fun setWrapLines(value: Boolean) {
        wrapLines.value = value
    }
}
