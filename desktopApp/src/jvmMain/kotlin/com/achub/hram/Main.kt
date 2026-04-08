package com.achub.hram

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.achub.hram.di.initKoin
import com.achub.hram.screen.main.Main

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HRAM – Heart Rate Activity Monitor",
        ) {
            Main()
        }
    }
}
