package com.mjdominiczak.songbook.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mjdominiczak.songbook.presentation.theme.SongbookTypography

@Composable
fun SimpleSectionView(
    text: String,
    chords: String? = null,
) {
    SectionWithMarker(text = text, chords = chords)
}

@Composable
fun ChorusSectionView(
    text: String,
    chords: String? = null,
) {
    SectionWithMarker(
        text = text,
        chords = chords,
        marker = "Ref.:",
        textStyle = SongbookTypography.chorusStyle
    )
}

@Composable
fun VerseSectionView(
    text: String,
    number: Int,
    chords: String? = null
) {
    SectionWithMarker(
        text = text,
        chords = chords,
        marker = "${number}.",
        textStyle = SongbookTypography.chorusStyle
    )
}

@Composable
private fun SectionWithMarker(
    text: String,
    chords: String? = null,
    marker: String? = null,
    textStyle: TextStyle = SongbookTypography.songStyle,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                TextLineWithChords(
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
private fun TextLineWithChords(
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
            )
        }
        Text(
            text = text,
            style = textStyle,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SimpleSectionPreview() {
    SimpleSectionView(
        text = """
            asd asda sda sd
            asd asda sda sda
            asd asda sda sd
            """.trimIndent(),
        chords = """
            A   F   A  B
            A   F   A  B
            A   F   A  B
            """.trimIndent(),
    )
}

@Preview(showBackground = true)
@Composable
private fun ChorusSectionPreview() {
    ChorusSectionView(
        text = """
            asd asda sda sd
            asd asda sda sda
            asd asda sda sd
            """.trimIndent(),
        chords = """
            A   F   A  B
            A   F   A  B
            A   F   A  B
            """.trimIndent(),
    )
}

@Preview(showBackground = true)
@Composable
private fun VerseSectionPreview() {
    VerseSectionView(
        text = """
            asd asda sda sd
            asd asda sda sda
            asd asda sda sd
            """.trimIndent(),
        number = 1,
        chords = """
            A   F   A  B
            A   F   A  B
            A   F   A  B
            """.trimIndent(),
    )
}
