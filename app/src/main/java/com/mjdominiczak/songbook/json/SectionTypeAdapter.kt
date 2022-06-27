package com.mjdominiczak.songbook.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mjdominiczak.songbook.data.Section

class SectionTypeAdapter : TypeAdapter<Section>() {
    override fun write(out: JsonWriter, value: Section) {
        when (value) {
            is Section.SimpleSection -> {
                out.value(value.text)
                out.value(value.chords)
            }
            is Section.Chorus -> {
                out.value(value.number)
                out.value(value.text)
                out.value(value.chords)
            }
            is Section.Verse -> {
                out.value(value.number)
                out.value(value.text)
                out.value(value.chords)
            }
        }
    }

    override fun read(reader: JsonReader): Section? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        reader.beginObject()
        var number: Int? = null
        var text = ""
        var chords: String? = null
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "number" -> number = reader.nextInt()
                "text" -> text = reader.nextString()
                "chords" -> {
                    if (reader.peek() == JsonToken.NULL) {
                        reader.nextNull()
                    } else {
                        chords = reader.nextString()
                    }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return when (number) {
            null -> Section.SimpleSection(text, chords)
            0 -> Section.Chorus(number, text, chords)
            else -> Section.Verse(number, text, chords)
        }
    }
}