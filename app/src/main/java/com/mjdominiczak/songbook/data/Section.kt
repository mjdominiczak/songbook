package com.mjdominiczak.songbook.data

sealed class Section(
    number: Int,
    text: String,
    chords: String? = null,
    val sectionId: Int
) {
    data class SimpleSection(
        val text: String,
        val chords: String? = null
    ) : Section(0, text, chords, 0)

    data class Verse(
        val number: Int,
        val text: String,
        val chords: String? = null
    ) : Section(number, text, chords, 0)

    data class Chorus(
        val number: Int = 0,
        val text: String,
        val chords: String? = null
    ) : Section(number, text, chords, 0)
}
