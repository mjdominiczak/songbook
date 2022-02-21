package com.mjdominiczak.songbook.data

data class Song(
    val id: Int,
    val version: Int,
    val title: String,
    val titleAlt: String,
    val info: String,
    val text: String,
    val chords: String,
    val transposition: Int,
    val tags: List<String>
)
