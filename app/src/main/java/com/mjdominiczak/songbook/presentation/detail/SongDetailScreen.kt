package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.presentation.components.Tag
import com.mjdominiczak.songbook.presentation.theme.SongbookTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    navController: NavController,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.song?.title.orEmpty(),
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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(vertical = 16.dp)
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
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    state.song.content?.forEach { section ->
                        when (section) {
                            is Section.SimpleSection -> SectionWithMarker(
                                text = section.text,
                                chords = section.chords
                            )
                            is Section.Chorus -> SectionWithMarker(
                                text = section.text,
                                chords = section.chords,
                                marker = "Ref.:",
                                textStyle = SongbookTypography.chorusStyle
                            )
                            is Section.Verse -> SectionWithMarker(
                                text = section.text,
                                chords = section.chords,
                                marker = "${section.number}."
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (tag in state.song.tags) {
                            Tag(tag = tag)
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

@Preview(showBackground = true)
@Composable
fun SectionWithMarker(
    text: String = """
            asd asda sda sd
            asd asda sda sda
            asd asda sda sd
            """.trimIndent(),
    chords: String? = """
            A   F   A  B
            A   F   A  B
            A   F   A  B
            """.trimIndent(),
    marker: String? = null,
    textStyle: TextStyle = SongbookTypography.songStyle,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val chordsLinesRaw = chords?.lines() ?: emptyList()
        marker?.let {
            Column {
                if (chordsLinesRaw.isNotEmpty()) Text(
                    text = "", // Distance for the first chords line
                    style = SongbookTypography.chordsStyle
                )
                Text(
                    text = it,
                    style = SongbookTypography.markerStyle
                )
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val lines = text.lines()
            val chordsLines = if (chordsLinesRaw.size == lines.size) {
                chordsLinesRaw
            } else {
                List(lines.size) { chordsLinesRaw.getOrNull(it) }
            }
            chordsLines.zip(lines).forEach { pair ->
                SectionSimple(
                    text = pair.second,
                    chords = pair.first,
                    textStyle = textStyle
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SectionSimple(
    text: String = "asd asda sda sd",
    chords: String? = "A   F   A  B",
    textStyle: TextStyle = SongbookTypography.songStyle
) {
    Column(
        modifier = Modifier.sizeIn(maxWidth = Dp.Infinity),
    ) {
        chords?.let {
            Text(
                text = it,
                style = SongbookTypography.chordsStyle,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
        )
    }
}
