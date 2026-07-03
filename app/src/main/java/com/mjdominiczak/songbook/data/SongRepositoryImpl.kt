package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
import com.mjdominiczak.songbook.domain.RefreshSongResult
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: SongApi,
    private val localDataSource: SongLocalDataSource,
) : SongRepository {
    override suspend fun addSong(song: Song) {
        TODO("Not yet implemented")
    }

    override fun observeAllSongs(): Flow<List<Song>> =
        localDataSource.observeAllSongs()

    override fun observeSongById(id: Int): Flow<Song?> =
        localDataSource.observeSongById(id)

    override suspend fun getAllSongs(): List<Song> {
        val cachedSongs = localDataSource.getAllSongs()
        return when (val result = refreshAllSongs()) {
            is RefreshAllSongsResult.Success -> result.songs
            is RefreshAllSongsResult.Failure -> cachedSongs.ifEmpty {
                throw IOException("Refresh failed: ${result.error}")
            }
        }
    }

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        try {
            val remoteSongs = api.getAllSongs()
            localDataSource.replaceAllSongs(remoteSongs)
            RefreshAllSongsResult.Success(remoteSongs)
        } catch (e: HttpException) {
            RefreshAllSongsResult.Failure(e.toRefreshSongsError())
        } catch (e: IOException) {
            RefreshAllSongsResult.Failure(e.toRefreshSongsError())
        }

    override suspend fun refreshSongById(id: Int): RefreshSongResult =
        try {
            val remoteSong = api.getSongById(id)
            localDataSource.upsertSong(remoteSong)
            RefreshSongResult.Success(remoteSong)
        } catch (e: HttpException) {
            RefreshSongResult.Failure(e.toRefreshSongsError())
        } catch (e: IOException) {
            RefreshSongResult.Failure(e.toRefreshSongsError())
        }

    override suspend fun getSongById(id: Int): Song {
        val cachedSong = localDataSource.getSongById(id)
        return when (val result = refreshSongById(id)) {
            is RefreshSongResult.Success -> result.song
            is RefreshSongResult.Failure -> cachedSong ?: throw IOException("Refresh failed: ${result.error}")
        }
    }

    private fun Throwable.toRefreshSongsError(): RefreshSongsError =
        when (this) {
            is SocketTimeoutException -> RefreshSongsError.Timeout
            is HttpException -> if (code() in 500..599) {
                RefreshSongsError.ServerUnavailable
            } else {
                RefreshSongsError.Unknown
            }
            is IOException -> RefreshSongsError.NetworkUnavailable
            else -> RefreshSongsError.Unknown
        }
}
