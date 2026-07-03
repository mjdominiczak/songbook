package com.mjdominiczak.songbook.domain

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ObserveSongUseCaseTest {

    private val repository = FakeObserveOneSongRepository()
    private val useCase = ObserveSongUseCase(repository)

    @Test
    fun invoke_emitsSavedSongFromRepositoryObservation() = runTest {
        val savedSong = song(id = 1, title = "Saved")
        repository.savedSong.value = savedSong

        val result = useCase(1).first()

        assertThat(result).isEqualTo(savedSong)
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeObserveOneSongRepository : SongRepository {
    val savedSong = MutableStateFlow<Song?>(null)

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by ObserveSongUseCaseTest")

    override fun observeAllSongs(): Flow<List<Song>> =
        error("observeAllSongs is not used by ObserveSongUseCaseTest")

    override fun observeSongById(id: Int): Flow<Song?> = savedSong

    override suspend fun getAllSongs(): List<Song> =
        error("getAllSongs is not used by ObserveSongUseCaseTest")

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        error("refreshAllSongs is not used by ObserveSongUseCaseTest")

    override suspend fun refreshSongById(id: Int): RefreshSongResult =
        error("refreshSongById is not used by ObserveSongUseCaseTest")

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by ObserveSongUseCaseTest")
}
