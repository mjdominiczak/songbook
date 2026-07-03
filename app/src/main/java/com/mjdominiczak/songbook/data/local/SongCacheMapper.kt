package com.mjdominiczak.songbook.data.local

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.Song
import javax.inject.Inject

class SongCacheMapper @Inject constructor(
    private val gson: Gson,
) {
    private val sectionAdapter = gson.getAdapter(Section::class.java)

    fun toEntity(song: Song, updatedAtMillis: Long = System.currentTimeMillis()) = SongEntity(
        id = song.id,
        version = song.version,
        title = song.title,
        titleAlt = song.titleAlt,
        info = song.info,
        contentJson = song.content?.let(::sectionsToJson),
        transposition = song.transposition,
        tagsJson = gson.toJson(song.tags.toTypedArray(), Array<String>::class.java),
        updatedAtMillis = updatedAtMillis,
    )

    fun fromEntity(entity: SongEntity) = Song(
        id = entity.id,
        version = entity.version,
        title = entity.title,
        titleAlt = entity.titleAlt,
        info = entity.info,
        content = entity.contentJson?.let(::sectionsFromJson),
        transposition = entity.transposition,
        tags = gson.fromJson(entity.tagsJson, Array<String>::class.java)?.toList() ?: emptyList(),
    )

    private fun sectionsToJson(sections: List<Section>): String {
        val array = JsonArray()
        sections.forEach { array.add(sectionAdapter.toJsonTree(it)) }
        return array.toString()
    }

    private fun sectionsFromJson(json: String): List<Section> =
        JsonParser.parseString(json)
            .asJsonArray
            .map(sectionAdapter::fromJsonTree)
}
