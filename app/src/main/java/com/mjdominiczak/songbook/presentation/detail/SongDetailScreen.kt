package com.mjdominiczak.songbook.presentation.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mjdominiczak.songbook.data.Section
import com.mjdominiczak.songbook.presentation.components.ChorusSectionView
import com.mjdominiczak.songbook.presentation.components.OptionWithSwitch
import com.mjdominiczak.songbook.presentation.components.SimpleSectionView
import com.mjdominiczak.songbook.presentation.components.Tag
import com.mjdominiczak.songbook.presentation.components.VerseSectionView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongDetailScreen(
    navController: NavController,
    viewModel: SongDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                            contentDescription = "Wstecz"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onSettingsClicked() }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ustawienia"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.song != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp)
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
                        state.song.content.forEach { section ->
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (viewModel.wrapLines) Modifier
                                        else Modifier.horizontalScroll(rememberScrollState())
                                    )
                                    .padding(horizontal = 16.dp),
                            ) {
                                fun Section.getChordsIfNeeded() =
                                    if (viewModel.displayChords) chords else null

                                when (section) {
                                    is Section.SimpleSection -> SimpleSectionView(
                                        text = section.text,
                                        chords = section.getChordsIfNeeded()
                                    )
                                    is Section.Chorus -> ChorusSectionView(
                                        text = section.text,
                                        chords = section.getChordsIfNeeded()
                                    )
                                    is Section.Verse -> VerseSectionView(
                                        text = section.text,
                                        number = section.number,
                                        chords = section.getChordsIfNeeded(),
                                    )
                                }
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
                }
                if (viewModel.showSettings) {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.onSettingsDismissed() },
                    ) {
                        OptionWithSwitch(
                            optionText = "Wyświetl akordy",
                            checked = viewModel.displayChords,
                            onCheckedChange = { viewModel.onDisplayChordsChanged(it) },
                        )
                        OptionWithSwitch(
                            optionText = "Zawijaj linie",
                            checked = viewModel.wrapLines,
                            onCheckedChange = { viewModel.onWrapLinesChanged(it) },
                        )
                        // FIXME: Fix for Modal Bottom Sheet appearing below navigation bar
                        //  https://issuetracker.google.com/issues/285166602
                        Spacer(modifier = Modifier.height(56.dp))
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
