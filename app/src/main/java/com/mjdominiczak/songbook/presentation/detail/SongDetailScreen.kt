package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.presentation.components.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    navController: NavController,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Text(
                        text = state.song?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.song != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row {
                        for (tag in state.song.tags) {
                            Tag(tag = tag, modifier = Modifier.padding(end = 8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(text = state.song.text)
                }
            } else {
                Text(
                    text = state.error ?: "Unexpected error occured",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}