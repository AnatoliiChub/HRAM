package com.achub.hram.style

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

@Composable
fun hramTextFieldColors() = with(MaterialTheme.colorScheme) {
    val containerColor = onBackground.copy(alpha = 0.04f)
    TextFieldDefaults.colors(
        cursorColor = primary,
        disabledTextColor = onBackground.copy(alpha = 0.6f),
        focusedIndicatorColor = onBackground.copy(alpha = 0.8f),
        unfocusedIndicatorColor = onBackground.copy(alpha = 0.3f),
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        errorContainerColor = containerColor,
        disabledContainerColor = containerColor,
    )
}
