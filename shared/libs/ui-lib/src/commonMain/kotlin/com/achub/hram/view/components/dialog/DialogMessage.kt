package com.achub.hram.view.components.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.LabelSmall

@Composable
fun DialogMessage(modifier: Modifier = Modifier, message: String) {
    Text(
        modifier = modifier,
        text = message,
        style = LabelSmall.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            fontWeight = W500
        ),
        textAlign = TextAlign.Center
    )
}

@Preview
@Composable
private fun DialogMessagePreview() {
    DialogMessage(message = "Sample dialog message")
}
