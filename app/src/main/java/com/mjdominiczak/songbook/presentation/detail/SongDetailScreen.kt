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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.data.Section
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
                        maxLines = 2,
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.song != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    state.song.info?.let {
                        Text(text = it, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Light)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    state.song.content?.forEach { section ->
                        when (section) {
                            is Section.SimpleSection -> Text(text = section.text)
                            is Section.Chorus -> Text(
                                buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontStyle = FontStyle.Italic
                                        )
                                    ) {
                                        append("Ref.: ")
                                    }
                                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                                        append(section.text)
                                    }
                                }
                            )
                            is Section.Verse -> Text(
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${section.number}. ")
                                    }
                                    append(section.text)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
}