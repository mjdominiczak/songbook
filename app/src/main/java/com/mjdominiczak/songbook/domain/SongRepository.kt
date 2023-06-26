package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song

interface SongRepository {

    suspend fun addSong(song: Song)

    suspend fun addMultipleSongs(songs: List<Song>)

    suspend fun getAllSongs(): List<Song>

    suspend fun getSongById(id: Int): Song

}