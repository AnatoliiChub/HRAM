package com.achub.hram.view.components

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.achub.hram.style.DarkRedShadow
import com.achub.hram.style.hramTextFieldColors
import kotlinx.coroutines.delay

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
    shape: Shape = TextFieldDefaults.shape,
) {
    var text by remember { mutableStateOf(value) }
    LaunchedEffect(text) {
        delay(TEXT_DEBOUNCE_TIME)
        onValueChange(text)
    }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = DarkRedShadow,
        backgroundColor = DarkRedShadow.copy(alpha = 0.4f)
    )
    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
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
            colors = hramTextFieldColors()
        )
    }
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
