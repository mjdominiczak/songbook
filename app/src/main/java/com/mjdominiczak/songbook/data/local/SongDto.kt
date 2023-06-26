package com.mjdominiczak.songbook.data.local

import com.mjdominiczak.songbook.data.Song
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
class SongDto : RealmObject {
    @PrimaryKey
    var id: Int = 0
    var version: Int = 0
    var title: String = ""
    var titleAlt: String? = null
    var info: String? = null
//    @Serializable(RealmListKSerializer::class)
    var content: RealmList<SectionDto> = realmListOf()
    var transposition: Int = 0
    var tags: RealmList<String> = realmListOf()
}

fun Song.toSongDto() = SongDto().apply {
    id = this@toSongDto.id
    version = this@toSongDto.version
    title = this@toSongDto.title
    titleAlt = this@toSongDto.titleAlt
    info = this@toSongDto.info
    content = this@toSongDto.content.map { it.toSectionDto() }.toRealmList()
    transposition = this@toSongDto.transposition
    tags = this@toSongDto.tags.toRealmList()
}

fun SongDto.toSong() = Song(
    id = this.id,
    version = this.version,
    title = this.title,
    titleAlt = this.titleAlt,
    info = this.info,
//    content = emptyList(),
    content = this.content.map { it.toSection() },
    transposition = this.transposition,
    tags = this.tags,
)
