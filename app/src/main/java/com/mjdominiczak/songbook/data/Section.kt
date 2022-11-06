package com.mjdominiczak.songbook.data

sealed class Section(
    open val number: Int,
    open val text: String,
    open val chords: String? = null,
    val sectionId: Int
) {
    data class SimpleSection(
        override val text: String,
        override val chords: String? = null
    ) : Section(0, text, chords, 0)

    data class Verse(
        override val number: Int,
        override val text: String,
        override val chords: String? = null
    ) : Section(number, text, chords, 0)

    data class Chorus(
        override val number: Int = 0,
        override val text: String,
        override val chords: String? = null
    ) : Section(number, text, chords, 0)
}
