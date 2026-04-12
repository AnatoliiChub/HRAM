package com.achub.hram.ext

import androidx.compose.runtime.Composable

@Suppress(names = ["ComposableNaming"])
@Composable
actual fun requestBluetooth(onRequested: () -> Unit) {
    onRequested()
    // Not required — iOS shows the Bluetooth permission dialog automatically.
}
