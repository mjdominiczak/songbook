package com.mjdominiczak.songbook.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mjdominiczak.songbook.presentation.components.Tag

@Composable
fun SongDetailScreen(viewModel: SongDetailViewModel = hiltViewModel()) {
    val state = viewModel.state.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.song != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(text = state.song.title, style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = state.song.text)
                Spacer(modifier = Modifier.height(20.dp))
                Row {
                    for (tag in state.song.tags) {
                        Tag(tag = tag, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }
        } else {
            Text(
                text = state.error ?: "Unexpected error occured",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}