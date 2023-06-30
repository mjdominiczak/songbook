package com.mjdominiczak.songbook.presentation.list

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun SongListScreen(
    navController: NavController,
    viewModel: SongListViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
//    var fabVisible by remember { mutableStateOf(true) }
//    val nestedScrollConnection = remember {
//        object : NestedScrollConnection {
//            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                val deltaY = available.y
//                fabVisible = deltaY >= 0
//                return Offset.Zero
//            }
//        }
//    }

    Scaffold(
//        modifier = Modifier.nestedScroll(nestedScrollConnection),
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
//        floatingActionButton = {
//            AnimatedVisibility(
//                visible = fabVisible,
//                enter = scaleIn(),
//                exit = scaleOut()
//            ) {
//                FloatingActionButton(onClick = { navController.navigate(Routes.ADD_SONG) }) {
//                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add new song")
//                }
//            }
//        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null || state.songs.isEmpty()) {
                InfoWithLoadButton(
                    text = state.error ?: stringResource(id = R.string.no_songs_available),
                    onClick = { viewModel.onLoadSongsClicked() }
                )
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

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InfoWithLoadButton(
    text: String = stringResource(id = R.string.no_songs_available),
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = text,
            color = contentColorFor(MaterialTheme.colorScheme.background),
            maxLines = 3,
            style = MaterialTheme.typography.titleMedium
        )
        Button(onClick = { onClick() }) {
            Text(text = stringResource(R.string.load_songs))
        }
    }
}

