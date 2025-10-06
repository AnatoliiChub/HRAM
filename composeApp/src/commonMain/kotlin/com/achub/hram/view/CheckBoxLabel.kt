package com.achub.hram.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import com.achub.hram.style.LabelLarge
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.style.White
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun CheckBoxLabel(
    title: String,
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: () -> Unit
) {
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
            colors = CheckboxDefaults.colors(checkedColor = Red, disabledCheckedColor = Red.copy(alpha = 0.1f)),
            enabled = isEnabled
        )
        val textColor = if (isEnabled) White else White.copy(alpha = 0.3f)
        Text(text = title, style = LabelLarge.copy(color = textColor))
    }
}

@Composable
fun HRCheckBoxLabel(
    isChecked: Boolean,
    isEnabled: Boolean,
    connectedDevice: String? = null,
    onCheckedChange: () -> Unit
) {
    Column(horizontalAlignment = CenterHorizontally) {
        CheckBoxLabel("Heart Rate tracking", isChecked, isEnabled, onCheckedChange)
        connectedDevice?.let {
            Text(
                text = "Device: $connectedDevice",
                style = LabelMedium.copy(color = White.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun LocationCheckBoxLabel(
    isChecked: Boolean,
    isEnabled: Boolean,
    onCheckedChange: () -> Unit
) {
    CheckBoxLabel("Location tracking", isChecked, isEnabled, onCheckedChange)
}

@Preview()
@Composable
private fun CheckBoxLabelPreview() {
    Column() {
        HRCheckBoxLabel(isChecked = true, isEnabled = true, connectedDevice = "Hr monitor", onCheckedChange = {})
        LocationCheckBoxLabel(isChecked = false, isEnabled = false, onCheckedChange = {})
    }
}
