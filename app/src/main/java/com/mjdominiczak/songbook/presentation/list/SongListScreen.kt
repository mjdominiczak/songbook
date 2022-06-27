package com.mjdominiczak.songbook.presentation.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.R
import com.mjdominiczak.songbook.presentation.components.InitialStickyHeader
import com.mjdominiczak.songbook.presentation.components.SongListItem
import com.mjdominiczak.songbook.presentation.components.SongbookAppBarWithSearch
import com.mjdominiczak.songbook.presentation.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun SongListScreen(
    navController: NavController,
    viewModel: SongListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var fabVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                fabVisible = deltaY >= 0
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
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
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = fabVisible,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = { navController.navigate(Routes.ADD_SONG) }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add new song")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                    viewModel.songsFiltered
                        .groupBy { it.title[0] }
                        .forEach { (initial, listOfSongs) ->
                            stickyHeader {
                                InitialStickyHeader(initial = initial)
                            }
                            itemsIndexed(listOfSongs) { index, song ->
                                SongListItem(
                                    song = song,
                                    onClick = { navController.navigate(Routes.songDetailRoute(song.id)) }
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
