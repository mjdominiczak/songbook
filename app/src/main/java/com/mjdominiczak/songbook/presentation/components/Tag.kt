package com.mjdominiczak.songbook.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun Tag(
    params: TagParams,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = shape
            )
            .clip(shape)
            .background(
                color = when (params.selected) {
                    true -> MaterialTheme.colorScheme.primaryContainer
                    false -> MaterialTheme.colorScheme.background
                },
            )
            .then(
                if (params.onClick != null) Modifier.clickable { params.onClick.invoke() }
                else Modifier
            )
            .padding(4.dp)
    ) {
        Text(
            text = params.name,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
        )
    }
}

data class TagParams(
    val name: String,
    val selected: Boolean = false,
    val onClick: (() -> Unit)? = null,
)
