package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
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

    override suspend fun getSongById(id: Int): Song {
        val cachedSong = localDataSource.getSongById(id)
        return try {
            api.getSongById(id).also { remoteSong ->
                localDataSource.upsertSong(remoteSong)
            }
        } catch (e: HttpException) {
            cachedSong ?: throw e
        } catch (e: IOException) {
            cachedSong ?: throw e
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
