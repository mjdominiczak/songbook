package com.mjdominiczak.songbook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SongEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class SongDatabase : RoomDatabase() {
    abstract val songDao: SongDao
}
