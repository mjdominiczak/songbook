package com.mjdominiczak.songbook.data

import com.google.common.truth.Truth.assertThat
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.data.remote.SongApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class SongRepositoryImplTest {

    private val api = FakeSongApi()
    private val localDataSource = FakeSongLocalDataSource()
    private val repository = SongRepositoryImpl(api, localDataSource)

    @Test
    fun getAllSongs_withSuccessfulNetwork_returnsRemoteSongsAndPersistsThem() = runTest {
        val remoteSongs = listOf(song(id = 1, title = "Remote"))
        api.allSongs = remoteSongs

        val result = repository.getAllSongs()

        assertThat(result).isEqualTo(remoteSongs)
        assertThat(localDataSource.getAllSongs()).isEqualTo(remoteSongs)
    }

    @Test
    fun getAllSongs_withCachedSongsAndNetworkFailure_returnsCachedSongs() = runTest {
        val cachedSongs = listOf(song(id = 2, title = "Cached"))
        localDataSource.replaceAllSongs(cachedSongs)
        api.allSongsError = IOException("No network")

        val result = repository.getAllSongs()

        assertThat(result).isEqualTo(cachedSongs)
    }

    @Test
    fun getAllSongs_withEmptyCacheAndNetworkFailure_throwsNetworkError() = runTest {
        api.allSongsError = IOException("No network")

        assertThrowsIOException {
            repository.getAllSongs()
        }
    }

    @Test
    fun getSongById_withSuccessfulNetwork_returnsRemoteSongAndPersistsIt() = runTest {
        val remoteSong = song(id = 3, title = "Remote detail")
        api.songById = remoteSong

        val result = repository.getSongById(3)

        assertThat(result).isEqualTo(remoteSong)
        assertThat(localDataSource.getSongById(3)).isEqualTo(remoteSong)
    }

    @Test
    fun getSongById_withCachedSongAndNetworkFailure_returnsCachedSong() = runTest {
        val cachedSong = song(id = 4, title = "Cached detail")
        localDataSource.upsertSong(cachedSong)
        api.songByIdError = IOException("No network")

        val result = repository.getSongById(4)

        assertThat(result).isEqualTo(cachedSong)
    }

    @Test
    fun getSongById_withEmptyCacheAndNetworkFailure_throwsNetworkError() = runTest {
        api.songByIdError = IOException("No network")

        assertThrowsIOException {
            repository.getSongById(5)
        }
    }

    private suspend fun assertThrowsIOException(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected IOException")
        } catch (_: IOException) {
        }
    }

    private fun song(id: Int, title: String) = Song(
        id = id,
        version = 1,
        title = title,
        tags = listOf("RRN 2022"),
    )
}

private class FakeSongApi : SongApi {
    var allSongs: List<Song> = emptyList()
    var allSongsError: IOException? = null
    var songById: Song? = null
    var songByIdError: IOException? = null

    override suspend fun getAllSongs(): List<Song> {
        allSongsError?.let { throw it }
        return allSongs
    }

    override suspend fun getSongById(id: Int): Song {
        songByIdError?.let { throw it }
        return songById ?: error("No fake song configured for id $id")
    }
}

private class FakeSongLocalDataSource : SongLocalDataSource {
    private val songs = linkedMapOf<Int, Song>()

    override suspend fun getAllSongs(): List<Song> = songs.values.toList()

    override suspend fun getSongById(id: Int): Song? = songs[id]

    override suspend fun replaceAllSongs(songs: List<Song>) {
        this.songs.clear()
        songs.forEach { this.songs[it.id] = it }
    }

    override suspend fun upsertSong(song: Song) {
        songs[song.id] = song
    }
}
