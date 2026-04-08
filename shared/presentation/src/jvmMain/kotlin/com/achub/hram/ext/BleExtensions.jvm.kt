package com.achub.hram.ext

import androidx.compose.runtime.Composable

/** No Bluetooth enable dialog on desktop; Kable throws at scan time if BT is unavailable. */
@Suppress("ComposableNaming")
@Composable
actual fun requestBluetooth() = Unit

/** Desktop has no OS-level BLE permission flow — return a no-op controller. */
@Composable
actual fun permissionController(): BlePermissionController = NoOpBlePermissionController

private object NoOpBlePermissionController : BlePermissionController {
    override suspend fun requestBleBefore(action: () -> Unit, onFailure: () -> Unit) = action()

    override fun openAppSettings() = Unit
}
