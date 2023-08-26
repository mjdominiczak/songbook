package com.mjdominiczak.songbook.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

@Composable
fun OptionWithSwitch(
    optionText: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(text = optionText)
    }
}

@Preview
@Composable
private fun OptionWithSwitchPreview(
    @PreviewParameter(OptionWithSwitchPreviewProvider::class) checked: Boolean
) {
    OptionWithSwitch(
        optionText = "Test option",
        checked = checked,
    )
}

private class OptionWithSwitchPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}