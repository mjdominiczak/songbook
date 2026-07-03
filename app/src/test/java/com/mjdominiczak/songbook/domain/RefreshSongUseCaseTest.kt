package com.mjdominiczak.songbook.domain

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshSongUseCaseTest {

    private val repository = FakeRefreshOneSongRepository()
    private val useCase = RefreshSongUseCase(repository)

    @Test
    fun invoke_refreshesSongThroughRepository() = runTest {
        repository.refreshResults.add(RefreshSongResult.Success)

        val result = useCase(1)

        assertThat(result).isEqualTo(RefreshSongResult.Success)
    }

    @Test
    fun invoke_withTransientFailure_retriesOnce() = runTest {
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.NetworkUnavailable)
        )
        repository.refreshResults.add(RefreshSongResult.Success)

        val result = useCase(2)

        assertThat(result).isEqualTo(RefreshSongResult.Success)
        assertThat(repository.refreshCalls).isEqualTo(2)
    }

    @Test
    fun invoke_withTwoTransientFailures_retriesTwice() = runTest {
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.Timeout)
        )
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        repository.refreshResults.add(RefreshSongResult.Success)

        val result = useCase(3)

        assertThat(result).isEqualTo(RefreshSongResult.Success)
        assertThat(repository.refreshCalls).isEqualTo(3)
    }

    @Test
    fun invoke_withRepeatedTransientFailure_returnsTypedFailureAfterRetry() = runTest {
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        repository.refreshResults.add(
            RefreshSongResult.Failure(RefreshSongsError.ServerUnavailable)
        )

        val result = useCase(4)

        assertThat(result).isEqualTo(
            RefreshSongResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        assertThat(repository.refreshCalls).isEqualTo(3)
    }

    @Test
    fun invoke_withUnknownFailure_doesNotRetry() = runTest {
        repository.refreshResults.add(RefreshSongResult.Failure(RefreshSongsError.Unknown))

        val result = useCase(5)

        assertThat(result).isEqualTo(RefreshSongResult.Failure(RefreshSongsError.Unknown))
        assertThat(repository.refreshCalls).isEqualTo(1)
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeRefreshOneSongRepository : SongRepository {
    val refreshResults = ArrayDeque<RefreshSongResult>()
    var refreshCalls = 0
        private set

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by RefreshSongUseCaseTest")

    override fun observeAllSongs(): Flow<List<Song>> =
        error("observeAllSongs is not used by RefreshSongUseCaseTest")

    override fun observeSongById(id: Int): Flow<Song?> =
        error("observeSongById is not used by RefreshSongUseCaseTest")

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        error("refreshAllSongs is not used by RefreshSongUseCaseTest")

    override suspend fun refreshSongById(id: Int): RefreshSongResult {
        refreshCalls++
        return if (refreshResults.isEmpty()) {
            RefreshSongResult.Failure(RefreshSongsError.Unknown)
        } else {
            refreshResults.removeFirst()
        }
    }
}
