package com.mjdominiczak.songbook.data.local

import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.json.SectionTypeAdapter
import org.junit.Test

class SongCacheMapperTest {

    private val mapper = SongCacheMapper(
        GsonBuilder()
            .registerTypeAdapter(Section::class.java, SectionTypeAdapter())
            .create()
    )

    @Test
    fun songWithSectionsAndTags_roundTripsThroughEntity() {
        val song = Song(
            id = 1,
            version = 2,
            title = "Title",
            titleAlt = "Alt title",
            info = "Info",
            content = listOf(
                Section.SimpleSection(text = "Simple", chords = "C G"),
                Section.Verse(number = 1, text = "Verse", chords = null),
                Section.Chorus(number = 0, text = "Chorus", chords = "a F"),
            ),
            transposition = 3,
            tags = listOf("RRN 2022", "Uwielbienie"),
        )

        val entity = mapper.toEntity(song, updatedAtMillis = 123L)
        val result = mapper.fromEntity(entity)

        assertThat(result).isEqualTo(song)
        assertThat(entity.updatedAtMillis).isEqualTo(123L)
    }

    @Test
    fun songWithNullContent_roundTripsThroughEntity() {
        val song = Song(
            id = 2,
            version = 1,
            title = "Title",
            content = null,
            tags = emptyList(),
        )

        val result = mapper.fromEntity(mapper.toEntity(song, updatedAtMillis = 456L))

        assertThat(result).isEqualTo(song)
    }
}
