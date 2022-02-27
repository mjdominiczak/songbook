package com.mjdominiczak.songbook.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.presentation.components.InitialStickyHeader
import com.mjdominiczak.songbook.presentation.components.SongListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListScreen(
    navController: NavController,
    viewModel: SongListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.getAllSongs() }) {
                    Text(text = "Jeszcze raz")
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                state.songs.forEach { (initial, listOfSongs) ->
                    stickyHeader {
                        InitialStickyHeader(initial = initial)
                    }
                    itemsIndexed(listOfSongs) { index, song ->
                        SongListItem(
                            song = song,
                            onClick = { navController.navigate("songs/${song.id}") }
                        )
                        if (index < listOfSongs.size - 1) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}