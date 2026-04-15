package com.achub.hram

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.achub.hram.di.initKoin
import com.achub.hram.screen.MainScreenDesktop
import java.awt.Dimension

private const val MIN_WIDTH = 1024
private const val MIN_HEIGHT = 768

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HRAM – Heart Rate Activity Monitor",
        ) {
            window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
            MainScreenDesktop()
        }
    }
}
