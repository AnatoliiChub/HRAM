package com.achub.hram.view.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview

const val TEXT_DEBOUNCE_TIME = 50L

@Composable
fun HramTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    isError: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    supportingText: @Composable (() -> Unit)? = null,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    shape: Shape = TextFieldDefaults.shape,
) {
    var text by remember { mutableStateOf(value) }
    LaunchedEffect(text) {
        delay(TEXT_DEBOUNCE_TIME)
        onValueChange(text)
    }
    TextField(
        modifier = modifier,
        value = text,
        onValueChange = { text = it },
        singleLine = singleLine,
        isError = isError,
        textStyle = textStyle,
        enabled = enabled,
        readOnly = readOnly,
        shape = shape,
        supportingText = supportingText,
        colors = colors
    )
}

@Preview
@Composable
private fun HramTextFieldPreview() {
    HramTextField(
        value = "Sample text",
        onValueChange = {},
        singleLine = true,
        supportingText = { Text("Helper text") }
    )
}
