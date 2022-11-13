package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Chord
import com.mjdominiczak.songbook.data.Song
import java.util.regex.Pattern

class ChordCollector(private val songs: List<Song>) {
    private val chordsSet = mutableSetOf<Chord>()

    fun parseChords() {
        songs.forEach { song ->
            song.content?.forEach { section ->
                section.chords?.split(Pattern.compile("[\\s\\[\\]()|,]+")).run {
                    this?.forEach { singleChord ->
                        Chord.fromString(singleChord)?.let { chordsSet.add(it) }
                    }
                }
            }
        }
        println(chordsSet.sorted())
    }

}