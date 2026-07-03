package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSongUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    operator fun invoke(id: Int): Flow<Song?> = songRepository.observeSongById(id)
}
