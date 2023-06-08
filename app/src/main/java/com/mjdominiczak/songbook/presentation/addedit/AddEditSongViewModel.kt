package com.mjdominiczak.songbook.presentation.addedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.domain.AddSongUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditSongViewModel @Inject constructor(
    private val addSongUseCase: AddSongUseCase,
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
            addSongUseCase(
                song = Song(
                    id = 0,
                    version = 0,
                    title = title,
                    content = listOf(Section.SimpleSection(text = text))
                )
            )
        }
    }
}