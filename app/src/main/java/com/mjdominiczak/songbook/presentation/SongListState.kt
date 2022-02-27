package com.mjdominiczak.songbook.presentation

import com.mjdominiczak.songbook.data.Song

data class SongListState(
    val isLoading: Boolean = false,
    val songs: Map<Char, List<Song>> = emptyMap(),
    val error: String? = null
)
