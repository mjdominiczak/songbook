package com.mjdominiczak.songbook.presentation.list

import com.mjdominiczak.songbook.data.Song

data class SongListState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val tagsFilter: Set<String> = setOf("RRN 2022")
)
