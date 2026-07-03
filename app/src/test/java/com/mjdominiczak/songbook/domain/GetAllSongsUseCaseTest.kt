package com.mjdominiczak.songbook.domain

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.common.Resource
import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class GetAllSongsUseCaseTest {

    private val repository = FakeSongRepository()
    private val useCase = GetAllSongsUseCase(repository)

    @Test
    fun invoke_withRepositorySongs_emitsLoadingThenSuccess() = runTest {
        val songs = listOf(song(id = 1, title = "Adoracja"))
        repository.allSongsResult = Result.success(songs)

        val results = useCase().toList()

        assertThat(results).hasSize(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[1]).isInstanceOf(Resource.Success::class.java)
        assertThat(results[1].data).isEqualTo(songs)
    }

    @Test
    fun invoke_withRepositoryNetworkFailure_emitsLoadingThenError() = runTest {
        repository.allSongsResult = Result.failure(IOException("No network"))

        val results = useCase().toList()

        assertThat(results).hasSize(2)
        assertThat(results[0]).isInstanceOf(Resource.Loading::class.java)
        assertThat(results[1]).isInstanceOf(Resource.Error::class.java)
        assertThat(results[1].message).isEqualTo("No network")
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeSongRepository : SongRepository {
    var allSongsResult: Result<List<Song>> = Result.success(emptyList())

    override suspend fun addSong(song: Song): Unit =
        error("addSong is not used by GetAllSongsUseCase")

    override suspend fun getAllSongs(): List<Song> =
        allSongsResult.getOrThrow()

    override suspend fun getSongById(id: Int): Song =
        error("getSongById is not used by GetAllSongsUseCase")
}
