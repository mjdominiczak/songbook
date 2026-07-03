package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    suspend fun addSong(song: Song)

    fun observeAllSongs(): Flow<List<Song>>

    suspend fun getAllSongs(): List<Song>

    suspend fun refreshAllSongs(): RefreshAllSongsResult

    suspend fun getSongById(id: Int): Song

}
