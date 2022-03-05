package com.mjdominiczak.songbook.presentation.navigation

object Routes {
    const val ARG_SONG_ID = "songId"

    const val SONGS_LIST = "songs"
    const val ADD_SONG = "songs/add"
    const val SONG_DETAIL = "songs/{$ARG_SONG_ID}"

    fun songDetailRoute(id: Int) = SONG_DETAIL.replace("{$ARG_SONG_ID}", id.toString())
}