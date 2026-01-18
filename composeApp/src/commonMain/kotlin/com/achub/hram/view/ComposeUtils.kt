package com.achub.hram.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.achub.hram.ext.logger
import hram.composeapp.generated.resources.Res

@Composable
fun shader(path: String): String? {
    var shaderText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(path) {
        logger("123123") { "Loading shader from path: $path" }
        shaderText = Res.readBytes(path).decodeToString()
    }
    return shaderText
}
