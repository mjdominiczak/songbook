package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.common.RefreshDiagnosticsLogger
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.domain.RefreshAllSongsResult
import com.mjdominiczak.songbook.domain.RefreshSongResult
import com.mjdominiczak.songbook.domain.RefreshSongsError
import com.mjdominiczak.songbook.domain.SongRepository
import kotlinx.coroutines.CancellationException
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

    override suspend fun refreshAllSongs(): RefreshAllSongsResult =
        try {
            RefreshDiagnosticsLogger.log("repository all-songs remote request started")
            val remoteSongs = api.getAllSongs()
            RefreshDiagnosticsLogger.log("repository all-songs remote request succeeded count=${remoteSongs.size}")
            if (remoteSongs.isEmpty()) {
                RefreshDiagnosticsLogger.log(
                    "repository all-songs empty response treated as ${RefreshSongsError.ServerUnavailable}"
                )
                RefreshAllSongsResult.Failure(RefreshSongsError.ServerUnavailable)
            } else {
                localDataSource.replaceAllSongs(remoteSongs)
                RefreshDiagnosticsLogger.log("repository all-songs cache replace succeeded count=${remoteSongs.size}")
                RefreshAllSongsResult.Success
            }
        } catch (e: HttpException) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository all-songs failed mappedError=$error", e)
            RefreshAllSongsResult.Failure(error)
        } catch (e: IOException) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository all-songs failed mappedError=$error", e)
            RefreshAllSongsResult.Failure(error)
        } catch (e: CancellationException) {
            RefreshDiagnosticsLogger.log("repository all-songs request cancelled", e)
            throw e
        } catch (e: Exception) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository all-songs failed mappedError=$error", e)
            RefreshAllSongsResult.Failure(error)
        }

    override suspend fun refreshSongById(id: Int): RefreshSongResult =
        try {
            RefreshDiagnosticsLogger.log("repository song id=$id remote request started")
            val remoteSong = api.getSongById(id)
            RefreshDiagnosticsLogger.log("repository song id=$id remote request succeeded")
            localDataSource.upsertSong(remoteSong)
            RefreshDiagnosticsLogger.log("repository song id=$id cache upsert succeeded")
            RefreshSongResult.Success
        } catch (e: HttpException) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository song id=$id failed mappedError=$error", e)
            RefreshSongResult.Failure(error)
        } catch (e: IOException) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository song id=$id failed mappedError=$error", e)
            RefreshSongResult.Failure(error)
        } catch (e: CancellationException) {
            RefreshDiagnosticsLogger.log("repository song id=$id request cancelled", e)
            throw e
        } catch (e: Exception) {
            val error = e.toRefreshSongsError()
            RefreshDiagnosticsLogger.log("repository song id=$id failed mappedError=$error", e)
            RefreshSongResult.Failure(error)
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
