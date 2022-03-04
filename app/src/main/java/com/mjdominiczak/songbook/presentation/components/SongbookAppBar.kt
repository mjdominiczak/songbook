package com.mjdominiczak.songbook.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mjdominiczak.songbook.R

@Composable
fun SongbookAppBarWithSearch(
    isSearchActive: Boolean,
    onSearchActivate: () -> Unit,
    onSearchDeactivate: () -> Unit,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onNavIconPressed: () -> Unit
) {
    Crossfade(targetState = isSearchActive) { showSearch ->
        if (!showSearch) {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = { onNavIconPressed() }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu button"
                        )
                    }
                },
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
            BackHandler {
                onSearchDeactivate()
            }
            Surface(
                modifier = Modifier.height(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                tonalElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onSearchDeactivate) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back button"
                        )
                    }
                    SearchView(searchQuery) { newQuery -> onSearchQueryChanged(newQuery) }
                }
            }
        }
    }
}