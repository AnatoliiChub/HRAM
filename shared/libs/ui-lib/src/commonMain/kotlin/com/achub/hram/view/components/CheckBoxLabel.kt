package com.achub.hram.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.LabelSmall
import hram.ui_lib.generated.resources.Res
import hram.ui_lib.generated.resources.connected_device_label
import hram.ui_lib.generated.resources.hr_checkbox_label
import org.jetbrains.compose.resources.stringResource

@Composable
private fun CheckBoxLabel(
    title: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.clickable {
            if (isEnabled) {
                onCheckedChange()
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = colorScheme.primary,
                uncheckedColor = colorScheme.onBackground.copy(alpha = 0.6f),
                disabledCheckedColor = colorScheme.primary.copy(alpha = 0.1f)
            ),
            enabled = isEnabled
        )
        val textColor = if (isEnabled) colorScheme.onBackground else colorScheme.onBackground.copy(alpha = 0.3f)
        Text(text = title, style = LabelMedium.copy(color = textColor))
    }
}

@Composable
fun HRCheckBoxLabel(
    isChecked: Boolean,
    isEnabled: Boolean,
    connectedDevice: String? = null,
    onCheckedChange: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = CenterHorizontally) {
        CheckBoxLabel(stringResource(Res.string.hr_checkbox_label), isChecked, isEnabled, onCheckedChange)
        connectedDevice?.let {
            Text(
                text = stringResource(Res.string.connected_device_label, connectedDevice),
                style = LabelSmall.copy(color = colorScheme.onBackground.copy(alpha = 0.3f))
            )
        }
    }
}

@Preview
@Composable
private fun HRCheckBoxLabelPreview() {
    HRCheckBoxLabel(
        isChecked = true,
        isEnabled = true,
        connectedDevice = "Polar H10",
        onCheckedChange = {},
    )
}
