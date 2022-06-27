package com.mjdominiczak.songbook.data

data class Song(
    val id: Int,
    val version: Int,
    val title: String,
    val titleAlt: String? = null,
    val info: String? = null,
    val content: List<Section>? = emptyList(),
    val transposition: Int = 0,
    val tags: List<String> = emptyList()
)
