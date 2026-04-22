package com.achub.hram.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkRed,
    onPrimary = Color.White,
    secondary = Red2,
    onSecondary = Color.White,
    background = Black,
    onBackground = White,
    surface = Dark,
    onSurface = White,
    error = LightRed,
    onError = Color.Black,
    onSurfaceVariant = DarkText.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = Red2,
    onSecondary = Color.White,
    background = OffWhite,
    onBackground = DarkText,
    surface = Color.White,
    onSurface = DarkText,
    error = Red2,
    onError = Color.White,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkText.copy(alpha = 0.7f)
)

@Composable
fun HramTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
