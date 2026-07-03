package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import javax.inject.Inject

class RefreshAllSongsUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(): List<Song> = songRepository.refreshAllSongs()
}
