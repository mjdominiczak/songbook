package com.mjdominiczak.songbook.data

data class Chord(
    val base: ChordBase,
    val suffix: String = ""
)

enum class ChordBase {
    C, Cis, D, Es, E, F, Fis, G, As, A, B, H
}