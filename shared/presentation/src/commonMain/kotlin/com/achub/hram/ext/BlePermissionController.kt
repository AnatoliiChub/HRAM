package com.achub.hram.ext

/**
 * Platform-agnostic abstraction over BLE permission handling.
 * Hides moko-permissions from commonMain so the JVM (desktop) target compiles
 * without a moko dependency.
 */
interface BlePermissionController {
    suspend fun requestBleBefore(action: () -> Unit, onFailure: () -> Unit)

    fun openAppSettings()
}
