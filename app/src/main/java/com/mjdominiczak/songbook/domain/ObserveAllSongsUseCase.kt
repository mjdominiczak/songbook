package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.data.Song
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAllSongsUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    operator fun invoke(): Flow<List<Song>> = songRepository.observeAllSongs()
}
