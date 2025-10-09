package com.achub.hram.view.dialog.base

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.text.style.TextAlign
import com.achub.hram.style.LabelMedium
import com.achub.hram.style.White80

@Composable
fun DialogMessage(modifier: Modifier = Modifier, message: String) {
    Text(
        text = message,
        style = LabelMedium.copy(color = White80, fontWeight = W500),
        textAlign = TextAlign.Center
    )
}
