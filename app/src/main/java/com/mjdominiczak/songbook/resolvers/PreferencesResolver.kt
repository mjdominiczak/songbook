package com.mjdominiczak.songbook.resolvers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mjdominiczak.songbook.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SongPreferences {
    val displayChords: Flow<Boolean>
    val wrapLines: Flow<Boolean>
    suspend fun setDisplayChords(value: Boolean)
    suspend fun setWrapLines(value: Boolean)
}

class PreferencesResolver(private val context: Context) : SongPreferences {

    override val displayChords: Flow<Boolean> = context.dataStore.data.map { it[DISPLAY_CHORDS] ?: true }
    override val wrapLines: Flow<Boolean> = context.dataStore.data.map { it[WRAP_LINES] ?: false }

    override suspend fun setDisplayChords(value: Boolean) {
        context.dataStore.edit {
            it[DISPLAY_CHORDS] = value
        }
    }

    override suspend fun setWrapLines(value: Boolean) {
        context.dataStore.edit {
            it[WRAP_LINES] = value
        }
    }

    companion object {
        private val DISPLAY_CHORDS = booleanPreferencesKey("display_chords")
        private val WRAP_LINES = booleanPreferencesKey("wrap_lines")
    }
}
