package com.mjdominiczak.songbook.domain

sealed class RefreshAllSongsResult {
    data object Success : RefreshAllSongsResult()
    data class Failure(val error: RefreshSongsError) : RefreshAllSongsResult()
}

sealed class RefreshSongResult {
    data object Success : RefreshSongResult()
    data class Failure(val error: RefreshSongsError) : RefreshSongResult()
}

enum class RefreshSongsError {
    Timeout,
    NetworkUnavailable,
    ServerUnavailable,
    Unknown,
}
