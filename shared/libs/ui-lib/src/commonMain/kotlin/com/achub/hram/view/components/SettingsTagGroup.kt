package com.achub.hram.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen4
import com.achub.hram.style.Dimen8
import com.achub.hram.style.LabelMedium

@Composable
fun <T> SettingsTagGroup(
    label: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = LabelMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(Dimen12))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimen8)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                TagItem(
                    label = optionLabel(option),
                    isSelected = isSelected,
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TagItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.onBackground else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
    val borderColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimen16))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(Dimen16))
            .clickable(onClick = onClick)
            .padding(vertical = Dimen8, horizontal = Dimen4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = LabelMedium,
            color = textColor
        )
    }
}
