package com.achub.hram.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.achub.hram.style.Dimen16
import com.achub.hram.style.Dimen24
import com.achub.hram.style.LabelLarge
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingsMenuItem(
    icon: DrawableResource,
    title: String,
    trailingIcon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onBackground,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    trailingIconTint: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(Dimen16),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(Dimen24),
            tint = iconTint
        )
        Spacer(Modifier.width(Dimen16))
        Text(
            text = title,
            style = LabelLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(trailingIcon),
            contentDescription = null,
            modifier = Modifier.size(Dimen24),
            tint = trailingIconTint
        )
    }
}
