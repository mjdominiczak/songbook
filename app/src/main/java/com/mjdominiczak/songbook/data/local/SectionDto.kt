package com.mjdominiczak.songbook.data.local

import com.mjdominiczak.songbook.data.Section
import io.realm.kotlin.types.RealmObject
import kotlinx.serialization.Serializable

@Serializable
class SectionDto : RealmObject {
    var number: Int = 0
    var text: String = ""
    var chords: String? = null
    var sectionId: Int = 0
}

fun Section.toSectionDto() = SectionDto().apply {
    number = this@toSectionDto.number
    text = this@toSectionDto.text
    chords = this@toSectionDto.chords
    sectionId = this@toSectionDto.sectionId
}

fun SectionDto.toSection() = when {
    number == 0 -> Section.Chorus(
        text = text,
        chords = chords,
    )
    number > 0 -> Section.Verse(
        number = number,
        text = text,
        chords = chords,
    )
    else -> Section.SimpleSection(
        text = text,
        chords = chords,
    )
}
