package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song

class AddSongUseCase(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(song: Song) = songRepository.addSong(song)
}
