package com.mjdominiczak.songbook.presentation

import com.mjdominiczak.songbook.data.Song

data class SongDetailState(
    val isLoading: Boolean = false,
    val song: Song? = null,
    val error: String? = null
)
