package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.local.SongDatabase
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: SongApi,
    private val db: SongDatabase
) : SongRepository {
    override suspend fun addSong(song: Song) {
        db.addSong(song)
    }

    override suspend fun getAllSongs(): List<Song> {
        return api.getAllSongs()
    }

    override suspend fun getSongById(id: Int): Song {
        return api.getSongById(id)
    }
}