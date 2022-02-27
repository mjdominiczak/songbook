package com.mjdominiczak.songbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mjdominiczak.songbook.data.Song
import com.mjdominiczak.songbook.presentation.theme.HeaderDark
import com.mjdominiczak.songbook.presentation.theme.HeaderLight

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Text(song.title, style = MaterialTheme.typography.subtitle1)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                for (tag in song.tags) {
                    Tag(tag = tag, modifier = Modifier.padding(end = 8.dp))
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
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun Tag(tag: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.secondary,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        Text(
            text = tag,
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2
        )
    }
}