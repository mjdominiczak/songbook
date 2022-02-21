package com.mjdominiczak.songbook.data

import com.mjdominiczak.songbook.data.remote.SongApi
import com.mjdominiczak.songbook.domain.SongRepository
import javax.inject.Inject

class SongRepositoryImpl @Inject constructor(
    private val api: SongApi
) : SongRepository {
    override suspend fun addSong(song: Song) {
        TODO("Not yet implemented")
    }

    override suspend fun getAllSongs(): List<Song> {
        return api.getAllSongs()
    }

    override suspend fun getSongById(id: Int): Song {
        return api.getSongById(id)
    }
}