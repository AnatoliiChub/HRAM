package com.achub.hram.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.Red
import com.achub.hram.style.White

@Composable
fun SettingsRangePicker(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = LabelMedium, color = White)
        Spacer(Modifier.height(Dimen8))
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = White,
                activeTrackColor = Red,
                inactiveTrackColor = White.copy(alpha = 0.5f)
            ),
            valueRange = range,
            modifier = Modifier.padding(vertical = Dimen8)
        )
    }
}
