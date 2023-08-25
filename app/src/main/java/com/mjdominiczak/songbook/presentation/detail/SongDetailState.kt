package com.mjdominiczak.songbook.presentation.detail

import com.mjdominiczak.songbook.data.Song

data class SongDetailState(
    val isLoading: Boolean = false,
    val song: Song? = null,
    val displayChords: Boolean = true,
    val error: String? = null,
)
