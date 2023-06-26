package com.mjdominiczak.songbook.presentation.addedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mjdominiczak.songbook.domain.AddSongUseCase
import com.mjdominiczak.songbook.json.SongsData
import com.mjdominiczak.songbook.resolvers.ResourcesResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors
import javax.inject.Inject

@HiltViewModel
class AddEditSongViewModel @Inject constructor(
    private val addSongUseCase: AddSongUseCase,
    private val resourcesResolver: ResourcesResolver,
    private val gson: Gson,
) : ViewModel() {

    var title by mutableStateOf("")
    var text by mutableStateOf("")

    fun onTitleChanged(newTitle: String) {
        title = newTitle
    }

    fun onTextChanged(newText: String) {
        text = newText
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            val inputStream = resourcesResolver.getAsset("RRN_2022.json")
            val json = BufferedReader(InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining())
//            val songs = Json.decodeFromString<SongsData>(json)
            val songs = gson.fromJson(json, SongsData::class.java)
            addSongUseCase(songs.data)
        }
    }
}