package com.mjdominiczak.songbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.presentation.theme.HeaderDark
import com.mjdominiczak.songbook.presentation.theme.HeaderLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Text(song.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                song.tags.forEach { tag ->
                    Tag(params = TagParams(name = tag))
                }
            }
        }
    }
}

@Composable
fun InitialStickyHeader(initial: Char) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isSystemInDarkTheme()) HeaderDark else HeaderLight)
            .padding(vertical = 2.dp, horizontal = 12.dp)
    ) {
        Text(
            text = initial.toString().uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
