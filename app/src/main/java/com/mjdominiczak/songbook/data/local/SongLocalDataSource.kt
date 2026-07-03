package com.mjdominiczak.songbook.data.local

import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface SongLocalDataSource {
    fun observeAllSongs(): Flow<List<Song>>
    fun observeSongById(id: Int): Flow<Song?>
    suspend fun getAllSongs(): List<Song>
    suspend fun getSongById(id: Int): Song?
    suspend fun replaceAllSongs(songs: List<Song>)
    suspend fun upsertSong(song: Song)
}

class RoomSongLocalDataSource @Inject constructor(
    private val songDao: SongDao,
    private val mapper: SongCacheMapper,
) : SongLocalDataSource {

    override fun observeAllSongs(): Flow<List<Song>> =
        songDao.observeAllSongs().map { songs -> songs.map(mapper::fromEntity) }

    override fun observeSongById(id: Int): Flow<Song?> =
        songDao.observeSongById(id).map { song -> song?.let(mapper::fromEntity) }

    override suspend fun getAllSongs(): List<Song> =
        songDao.getAllSongs().map(mapper::fromEntity)

    override suspend fun getSongById(id: Int): Song? =
        songDao.getSongById(id)?.let(mapper::fromEntity)

    override suspend fun replaceAllSongs(songs: List<Song>) {
        songDao.replaceAllSongs(songs.map(mapper::toEntity))
    }

    override suspend fun upsertSong(song: Song) {
        songDao.insertSong(mapper.toEntity(song))
    }
}
