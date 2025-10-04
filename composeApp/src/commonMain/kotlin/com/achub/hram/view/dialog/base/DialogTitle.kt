package com.achub.hram.view.dialog.base

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import com.achub.hram.style.LabelLarge
import com.achub.hram.style.White

@Composable
fun DialogTitle(modifier: Modifier, title: String) {
    Text(
        text = title,
        style = LabelLarge.copy(color = White, fontWeight = W600),
        textAlign = TextAlign.Center
    )
}