package com.achub.hram.view.components.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.Dimen12
import com.achub.hram.style.Dimen24
import com.achub.hram.style.LabelMedium
import com.achub.hram.view.components.HrButton

@Composable
fun DialogButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    HrButton(
        modifier = Modifier.wrapContentSize(),
        onClick = onClick,
        enabled = enabled,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = Dimen24, vertical = Dimen12),
            text = text,
            style = LabelMedium.copy(color = colorScheme.primary, fontWeight = W600),
        )
    }
}

@Preview
@Composable
private fun DialogButtonPreview() {
    DialogButton(text = "OK", onClick = {})
}
