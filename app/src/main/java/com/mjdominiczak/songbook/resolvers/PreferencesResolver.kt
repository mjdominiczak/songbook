package com.mjdominiczak.songbook.resolvers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mjdominiczak.songbook.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesResolver(private val context: Context) {

    val displayChords: Flow<Boolean> = context.dataStore.data.map { it[DISPLAY_CHORDS] ?: true }
    val wrapLines: Flow<Boolean> = context.dataStore.data.map { it[WRAP_LINES] ?: false }

    suspend fun setDisplayChords(value: Boolean) {
        context.dataStore.edit {
            it[DISPLAY_CHORDS] = value
        }
    }

    suspend fun setWrapLines(value: Boolean) {
        context.dataStore.edit {
            it[WRAP_LINES] = value
        }
    }

    companion object {
        private val DISPLAY_CHORDS = booleanPreferencesKey("display_chords")
        private val WRAP_LINES = booleanPreferencesKey("wrap_lines")
    }
}