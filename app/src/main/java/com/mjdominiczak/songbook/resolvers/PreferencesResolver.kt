package com.mjdominiczak.songbook.resolvers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.mjdominiczak.songbook.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesResolver(private val context: Context) {

    val displayChords: Flow<Boolean> = context.dataStore.data.map { it[DISPLAY_CHORDS] ?: true }

    suspend fun setDisplayChords(value: Boolean) {
        context.dataStore.edit {
            it[DISPLAY_CHORDS] = value
        }
    }

    companion object {
        private val DISPLAY_CHORDS = booleanPreferencesKey("display_chords")
    }
}