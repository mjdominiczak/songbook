package com.mjdominiczak.songbook.presentation.list

import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.RefreshSongsError

data class SongListState(
    val songs: List<Song> = emptyList(),
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val blockingError: RefreshSongsError? = null,
    val nonBlockingRefreshError: RefreshSongsError? = null,
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val tagsFilter: Set<String> = setOf("RRN 2022")
) {
    val isLoading: Boolean
        get() = isInitialLoading
}
