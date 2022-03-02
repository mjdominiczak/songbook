package com.mjdominiczak.songbook.presentation

import com.mjdominiczak.songbook.data.Song

data class SongListState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = ""
)
