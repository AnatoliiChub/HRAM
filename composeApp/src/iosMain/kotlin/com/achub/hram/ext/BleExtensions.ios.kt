package com.achub.hram.ext

import androidx.compose.runtime.Composable

@Suppress(names = ["ComposableNaming"])
@Composable
actual fun requestBluetooth() {
    // Does not required, since iOS shows request bluetooth  dialog automatically
}
