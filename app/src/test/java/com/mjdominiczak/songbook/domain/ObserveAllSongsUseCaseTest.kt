package com.mjdominiczak.songbook.domain

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveAllSongsUseCaseTest {

    private val repository = FakeObserveSongRepository()
    private val useCase = ObserveAllSongsUseCase(repository)

    @Test
    fun invoke_emitsSavedSongsFromRepositoryObservation() = runTest {
        val savedSongs = listOf(song(id = 1, title = "Saved"))
        repository.savedSongs.value = savedSongs

        val result = useCase().first()

        assertThat(result).isEqualTo(savedSongs)
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeObserveSongRepository : SongRepository {
    val savedSongs = MutableStateFlow<List<Song>>(emptyList())

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by ObserveAllSongsUseCaseTest")

    override fun observeAllSongs(): Flow<List<Song>> = savedSongs

    override fun observeSongById(id: Int): Flow<Song?> =
        error("observeSongById is not used by ObserveAllSongsUseCaseTest")

    override suspend fun getAllSongs(): List<Song> =
        error("getAllSongs is not used by ObserveAllSongsUseCaseTest")

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        error("refreshAllSongs is not used by ObserveAllSongsUseCaseTest")

    override suspend fun refreshSongById(id: Int): RefreshSongResult =
        error("refreshSongById is not used by ObserveAllSongsUseCaseTest")

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by ObserveAllSongsUseCaseTest")
}
