package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.common.RefreshDiagnosticsLogger
import kotlinx.coroutines.delay
import javax.inject.Inject

class RefreshSongUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(id: Int): RefreshSongResult {
        RefreshDiagnosticsLogger.log("song id=$id retry loop started")
        repeat(RETRY_LIMIT + 1) { attempt ->
            val attemptNumber = attempt + 1
            RefreshDiagnosticsLogger.log("song id=$id attempt $attemptNumber started")
            when (val result = songRepository.refreshSongById(id)) {
                is RefreshSongResult.Success -> {
                    RefreshDiagnosticsLogger.log("song id=$id attempt $attemptNumber succeeded")
                    return result
                }
                is RefreshSongResult.Failure -> {
                    val shouldRetry = attempt != RETRY_LIMIT && result.error.isTransient()
                    RefreshDiagnosticsLogger.log(
                        "song id=$id attempt $attemptNumber failed error=${result.error} shouldRetry=$shouldRetry"
                    )
                    if (attempt == RETRY_LIMIT || !result.error.isTransient()) {
                        return result
                    }
                    RefreshDiagnosticsLogger.log("song id=$id waiting ${RETRY_DELAY_MILLIS}ms before retry")
                    delay(RETRY_DELAY_MILLIS)
                }
            }
        }
        RefreshDiagnosticsLogger.log("song id=$id retry loop exhausted without result")
        return RefreshSongResult.Failure(RefreshSongsError.Unknown)
    }

    private fun RefreshSongsError.isTransient(): Boolean =
        this == RefreshSongsError.Timeout ||
            this == RefreshSongsError.NetworkUnavailable ||
            this == RefreshSongsError.ServerUnavailable

    private companion object {
        const val RETRY_LIMIT = 2
        const val RETRY_DELAY_MILLIS = 1500L
    }
}
