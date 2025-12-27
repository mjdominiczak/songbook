package com.mjdominiczak.songbook.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mjdominiczak.songbook.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongbookAppBarWithSearch(
    isSearchActive: Boolean,
    onSearchActivate: () -> Unit,
    onSearchDeactivate: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onNavIconPressed: () -> Unit
) {
    Crossfade(targetState = isSearchActive, label = "") { showSearch ->
        if (!showSearch) {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
//                navigationIcon = {
//                    IconButton(onClick = { onNavIconPressed() }) {
//                        Icon(
//                            imageVector = Icons.Default.Menu,
//                            contentDescription = "Menu button"
//                        )
//                    }
//                },
                actions = {
                    IconButton(onClick = onSearchActivate) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search button"
                        )
                    }
                }
            )
        } else {
            BackHandler { onSearchDeactivate() }
            val backgroundColor = MaterialTheme.colorScheme.secondaryContainer // TODO: Should it be provided in other way???
            CompositionLocalProvider(LocalContentColor provides contentColorFor(backgroundColor)) {
                Row(
                    modifier = Modifier
                        .background(backgroundColor)
                        .heightIn(min = TopAppBarDefaults.TopAppBarExpandedHeight)
                        .statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onSearchDeactivate) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back button"
                        )
                    }
                    SearchView(searchQuery) { newQuery -> onSearchQueryChanged(newQuery) }
                }
            }
        }
    }
}