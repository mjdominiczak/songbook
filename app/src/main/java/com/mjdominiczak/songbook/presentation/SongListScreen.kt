package com.mjdominiczak.songbook.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.R
import com.mjdominiczak.songbook.presentation.components.InitialStickyHeader
import com.mjdominiczak.songbook.presentation.components.SongListItem
import com.mjdominiczak.songbook.presentation.components.SongbookAppBarWithSearch
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(
    navController: NavController,
    viewModel: SongListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SongbookAppBarWithSearch(
                isSearchActive = state.isSearchActive,
                onSearchActivate = { viewModel.activateSearch() },
                onSearchDeactivate = { viewModel.deactivateSearch() },
                searchQuery = state.searchQuery,
                onSearchQueryChanged = { query -> viewModel.onSearchQueryChanged(query) },
                onNavIconPressed = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Not yet implemented")
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.getAllSongs() }) {
                        Text(text = stringResource(R.string.retry))
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    viewModel.songsFiltered.forEach { (initial, listOfSongs) ->
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
}
