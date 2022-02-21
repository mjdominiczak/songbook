package com.mjdominiczak.songbook.domain

class GetSongUseCase(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(id: Int) = songRepository.getSongById(id)
}