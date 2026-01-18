package com.achub.hram.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import hram.composeapp.generated.resources.Res

@Composable
fun shader(path: String): String? {
    var shaderText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(path) {
        shaderText = Res.readBytes(path).decodeToString()
    }
    return shaderText
}
