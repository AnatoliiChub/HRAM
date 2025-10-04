package com.achub.hram.view.dialog.base

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.achub.hram.style.LabelLarge
import com.achub.hram.style.Red
import com.achub.hram.style.White

@Composable
fun DialogButton(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonColors(
            containerColor = Red,
            contentColor = White,
            disabledContainerColor = Red.copy(alpha = 0.25f),
            disabledContentColor = White.copy(alpha = 0.25f)
        )
    ) {
        Text(text = text, style = LabelLarge)
    }
}