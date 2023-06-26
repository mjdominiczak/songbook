package com.mjdominiczak.songbook.json

import com.mjdominiczak.songbook.data.Song
import kotlinx.serialization.Serializable

@Serializable
data class SongsData(
    val data: List<Song>
)
