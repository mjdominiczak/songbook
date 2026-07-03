package com.mjdominiczak.songbook.domain

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RefreshAllSongsUseCaseTest {

    private val repository = FakeRefreshSongRepository()
    private val useCase = RefreshAllSongsUseCase(repository)

    @Test
    fun invoke_refreshesSongsThroughRepository() = runTest {
        val remoteSongs = listOf(song(id = 1, title = "Remote"))
        repository.refreshResults.add(RefreshAllSongsResult.Success(remoteSongs))

        val result = useCase()

        assertThat(result).isEqualTo(RefreshAllSongsResult.Success(remoteSongs))
    }

    @Test
    fun invoke_withTransientFailure_retriesOnce() = runTest {
        val remoteSongs = listOf(song(id = 2, title = "Remote after retry"))
        repository.refreshResults.add(
            RefreshAllSongsResult.Failure(RefreshSongsError.NetworkUnavailable)
        )
        repository.refreshResults.add(RefreshAllSongsResult.Success(remoteSongs))

        val result = useCase()

        assertThat(result).isEqualTo(RefreshAllSongsResult.Success(remoteSongs))
        assertThat(repository.refreshCalls).isEqualTo(2)
    }

    @Test
    fun invoke_withRepeatedTransientFailure_returnsTypedFailureAfterRetry() = runTest {
        repository.refreshResults.add(
            RefreshAllSongsResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        repository.refreshResults.add(
            RefreshAllSongsResult.Failure(RefreshSongsError.ServerUnavailable)
        )

        val result = useCase()

        assertThat(result).isEqualTo(
            RefreshAllSongsResult.Failure(RefreshSongsError.ServerUnavailable)
        )
        assertThat(repository.refreshCalls).isEqualTo(2)
    }

    @Test
    fun invoke_withUnknownFailure_doesNotRetry() = runTest {
        repository.refreshResults.add(RefreshAllSongsResult.Failure(RefreshSongsError.Unknown))

        val result = useCase()

        assertThat(result).isEqualTo(RefreshAllSongsResult.Failure(RefreshSongsError.Unknown))
        assertThat(repository.refreshCalls).isEqualTo(1)
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeRefreshSongRepository : SongRepository {
    val refreshResults = ArrayDeque<RefreshAllSongsResult>()
    var refreshCalls = 0
        private set

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by RefreshAllSongsUseCaseTest")

    override fun observeAllSongs(): Flow<List<Song>> =
        error("observeAllSongs is not used by RefreshAllSongsUseCaseTest")

    override suspend fun getAllSongs(): List<Song> =
        error("getAllSongs is not used by RefreshAllSongsUseCaseTest")

    override suspend fun refreshAllSongs(): RefreshAllSongsResult {
        refreshCalls++
        return if (refreshResults.isEmpty()) {
            RefreshAllSongsResult.Success(emptyList())
        } else {
            refreshResults.removeFirst()
        }
    }

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by RefreshAllSongsUseCaseTest")
}
