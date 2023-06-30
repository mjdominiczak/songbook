package com.mjdominiczak.songbook.data.local

import com.mjdominiczak.songbook.data.Song
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import kotlinx.coroutines.runBlocking

class SongDatabase(private val app: App) {

    private var realm: Realm

    init {
        runBlocking {
            val user = app.login(Credentials.anonymous())
            val config = SyncConfiguration.Builder(
                user = user,
                schema = setOf(SongDto::class, SectionDto::class)
            ).build()
            realm = Realm.open(config)
        }
    }

    suspend fun addSong(song: Song) = realm.write {
        copyToRealm(instance = song.toSongDto(), updatePolicy = UpdatePolicy.ALL)
    }

    suspend fun addSongs(songs: List<Song>) = songs.forEach { addSong(it) }

    fun getAllSongs() = realm.query<SongDto>().find()

    fun getSongById(id: Int) = realm.query<SongDto>("id = $id").find()

    fun close() = realm.close()
}