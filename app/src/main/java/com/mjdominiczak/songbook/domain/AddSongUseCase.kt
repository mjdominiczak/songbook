package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import javax.inject.Inject

class AddSongUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(songs: List<Song>) {
        songRepository.addMultipleSongs(songs)
    }
}
