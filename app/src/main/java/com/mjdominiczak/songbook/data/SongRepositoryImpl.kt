package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.local.SongDatabase
import com.mjdominiczak.songbook.data.local.toSong
import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: SongApi,
    private val db: SongDatabase
) : SongRepository {

    companion object {
        const val API_ENABLED = false
    }

    override suspend fun addSong(song: Song) {
        db.addSong(song)
    }

    override suspend fun getAllSongs(): List<Song> =
        if (API_ENABLED) api.getAllSongs()
        else db.getAllSongs().toList().map { it.toSong() }

    override suspend fun getSongById(id: Int): Song {
        return api.getSongById(id)
    }
}