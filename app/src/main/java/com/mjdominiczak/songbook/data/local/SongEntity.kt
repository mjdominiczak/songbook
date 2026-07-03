package com.mjdominiczak.songbook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Int,
    val version: Int,
    val title: String,
    val titleAlt: String?,
    val info: String?,
    val contentJson: String?,
    val transposition: Int,
    val tagsJson: String,
    val updatedAtMillis: Long,
)
