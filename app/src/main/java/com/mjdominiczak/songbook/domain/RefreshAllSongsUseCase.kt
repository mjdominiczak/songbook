package com.mjdominiczak.songbook.domain

import com.mjdominiczak.songbook.common.RefreshDiagnosticsLogger
import kotlinx.coroutines.delay
import javax.inject.Inject

class RefreshAllSongsUseCase @Inject constructor(
    private val songRepository: SongRepository,
) {
    suspend operator fun invoke(): RefreshAllSongsResult {
        RefreshDiagnosticsLogger.log("all-songs retry loop started")
        repeat(RETRY_LIMIT + 1) { attempt ->
            val attemptNumber = attempt + 1
            RefreshDiagnosticsLogger.log("all-songs attempt $attemptNumber started")
            when (val result = songRepository.refreshAllSongs()) {
                is RefreshAllSongsResult.Success -> {
                    RefreshDiagnosticsLogger.log("all-songs attempt $attemptNumber succeeded")
                    return result
                }
                is RefreshAllSongsResult.Failure -> {
                    val shouldRetry = attempt != RETRY_LIMIT && result.error.isTransient()
                    RefreshDiagnosticsLogger.log(
                        "all-songs attempt $attemptNumber failed error=${result.error} shouldRetry=$shouldRetry"
                    )
                    if (attempt == RETRY_LIMIT || !result.error.isTransient()) {
                        return result
                    }
                    RefreshDiagnosticsLogger.log("all-songs waiting ${RETRY_DELAY_MILLIS}ms before retry")
                    delay(RETRY_DELAY_MILLIS)
                }
            }
        }
        RefreshDiagnosticsLogger.log("all-songs retry loop exhausted without result")
        return RefreshAllSongsResult.Failure(RefreshSongsError.Unknown)
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
