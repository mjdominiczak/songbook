package com.mjdominiczak.songbook.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Not yet implemented")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu button"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Not yet implemented")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search button"
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior { true }
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
                LazyColumn {
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
}