package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    suspend fun addSong(song: Song)

    fun observeAllSongs(): Flow<List<Song>>

    fun observeSongById(id: Int): Flow<Song?>

    suspend fun getAllSongs(): List<Song>

    suspend fun refreshAllSongs(): RefreshAllSongsResult

    suspend fun refreshSongById(id: Int): RefreshSongResult

    suspend fun getSongById(id: Int): Song

}
