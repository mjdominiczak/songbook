package com.mjdominiczak.songbook.domain

import kotlinx.coroutines.delay
import javax.inject.Inject

class RefreshSongUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(id: Int): RefreshSongResult {
        repeat(RETRY_LIMIT + 1) { attempt ->
            when (val result = songRepository.refreshSongById(id)) {
                is RefreshSongResult.Success -> return result
                is RefreshSongResult.Failure -> {
                    if (attempt == RETRY_LIMIT || !result.error.isTransient()) {
                        return result
                    }
                    delay(RETRY_DELAY_MILLIS)
                }
            }
        }
        return RefreshSongResult.Failure(RefreshSongsError.Unknown)
    }

    private fun RefreshSongsError.isTransient(): Boolean =
        this == RefreshSongsError.Timeout ||
            this == RefreshSongsError.NetworkUnavailable ||
            this == RefreshSongsError.ServerUnavailable

    private companion object {
        const val RETRY_LIMIT = 1
        const val RETRY_DELAY_MILLIS = 1500L
    }
}
