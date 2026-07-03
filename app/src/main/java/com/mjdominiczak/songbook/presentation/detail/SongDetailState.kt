package com.mjdominiczak.songbook.presentation.detail

import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.RefreshSongsError

data class SongDetailState(
    val song: Song? = null,
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val blockingError: RefreshSongsError? = null,
    val displayChords: Boolean = true,
) {
    val isLoading: Boolean
        get() = isInitialLoading
}
