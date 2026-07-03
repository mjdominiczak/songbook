package com.mjdominiczak.songbook.domain

import kotlinx.coroutines.delay
import javax.inject.Inject

class RefreshAllSongsUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(): RefreshAllSongsResult {
        repeat(RETRY_LIMIT + 1) { attempt ->
            when (val result = songRepository.refreshAllSongs()) {
                is RefreshAllSongsResult.Success -> return result
                is RefreshAllSongsResult.Failure -> {
                    if (attempt == RETRY_LIMIT || !result.error.isTransient()) {
                        return result
                    }
                    delay(RETRY_DELAY_MILLIS)
                }
            }
        }
        return RefreshAllSongsResult.Failure(RefreshSongsError.Unknown)
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
