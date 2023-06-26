package com.mjdominiczak.songbook.data.local

import com.mjdominiczak.songbook.data.Song
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

class SongDatabase(private val realm: Realm) {

    suspend fun addSong(song: Song) = realm.write {
        copyToRealm(instance = song.toSongDto(), updatePolicy = UpdatePolicy.ALL)
    }

    suspend fun addSongs(songs: List<Song>) = songs.forEach { addSong(it) }

    fun getAllSongs() = realm.query<SongDto>().find()

    fun getSongById(id: Int) = realm.query<SongDto>("id = $id").find()
}