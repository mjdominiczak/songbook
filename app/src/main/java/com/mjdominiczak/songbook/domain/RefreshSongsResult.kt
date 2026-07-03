package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song

sealed class RefreshAllSongsResult {
    data class Success(val songs: List<Song>) : RefreshAllSongsResult()
    data class Failure(val error: RefreshSongsError) : RefreshAllSongsResult()
}

sealed class RefreshSongResult {
    data class Success(val song: Song) : RefreshSongResult()
    data class Failure(val error: RefreshSongsError) : RefreshSongResult()
}

enum class RefreshSongsError {
    Timeout,
    NetworkUnavailable,
    ServerUnavailable,
    Unknown,
}
