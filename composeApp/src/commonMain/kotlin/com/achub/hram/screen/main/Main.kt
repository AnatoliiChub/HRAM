package com.achub.hram.screen.main

import androidx.compose.runtime.Composable
import appStateChanged
import com.achub.hram.tracking.TrackingController
import org.koin.mp.KoinPlatform.getKoin

@Composable
fun Main() {
    appStateChanged { state ->
        if (state.isBackground()) return@appStateChanged
        getKoin().get<TrackingController>().onAppForeground()
    }
    MainScreen()
}
