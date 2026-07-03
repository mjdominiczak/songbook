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
        repository.refreshedSongs = remoteSongs

        val result = useCase()

        assertThat(result).isEqualTo(remoteSongs)
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeRefreshSongRepository : SongRepository {
    var refreshedSongs: List<Song> = emptyList()

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by RefreshAllSongsUseCaseTest")

    override fun observeAllSongs(): Flow<List<Song>> =
        error("observeAllSongs is not used by RefreshAllSongsUseCaseTest")

    override suspend fun getAllSongs(): List<Song> =
        error("getAllSongs is not used by RefreshAllSongsUseCaseTest")

    override suspend fun refreshAllSongs(): List<Song> = refreshedSongs

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by RefreshAllSongsUseCaseTest")
}
