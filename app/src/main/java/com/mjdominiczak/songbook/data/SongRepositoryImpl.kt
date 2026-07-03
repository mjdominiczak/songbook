package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.data.local.SongLocalDataSource
import com.mjdominiczak.songbook.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
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
        return try {
            refreshAllSongs()
        } catch (e: HttpException) {
            cachedSongs.ifEmpty { throw e }
        } catch (e: IOException) {
            cachedSongs.ifEmpty { throw e }
        }
    }

    override suspend fun refreshAllSongs(): List<Song> =
        api.getAllSongs().also { remoteSongs ->
            localDataSource.replaceAllSongs(remoteSongs)
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
}
