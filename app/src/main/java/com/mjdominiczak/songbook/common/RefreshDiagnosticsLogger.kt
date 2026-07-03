package com.mjdominiczak.songbook.common

import android.util.Log
import com.mjdominiczak.songbook.BuildConfig

object RefreshDiagnosticsLogger {
    private const val TAG = "SongbookRefreshDebug"
    private const val PREFIX = "[DEBUG-refresh]"

    fun log(message: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) return

        runCatching {
            if (throwable == null) {
                Log.d(TAG, "$PREFIX $message")
            } else {
                Log.d(TAG, "$PREFIX $message", throwable)
            }
        }
    }
}
