package com.achub.hram.style

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

private val containerColor = White.copy(alpha = 0.04f)

@Composable
fun hramTextFieldColors() = TextFieldDefaults.colors(
    cursorColor = Red,
    disabledTextColor = White.copy(alpha = 0.6f),
    focusedIndicatorColor = White.copy(alpha = 0.8f),
    unfocusedIndicatorColor = White.copy(alpha = 0.3f),
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    errorContainerColor = containerColor,
    disabledContainerColor = containerColor,
)
