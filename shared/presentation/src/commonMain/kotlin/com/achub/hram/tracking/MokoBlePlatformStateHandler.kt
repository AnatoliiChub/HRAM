package com.achub.hram.tracking

import com.achub.hram.data.models.ScanError
import dev.icerock.moko.permissions.DeniedException

/**
 * moko-permissions-aware implementation of [BlePlatformStateHandler].
 * Recognises [DeniedException] with the "Bluetooth is powered off" message that moko
 * raises on both platforms when the system Bluetooth adapter is disabled.
 */
class MokoBlePlatformStateHandler : BlePlatformStateHandler {
    override fun mapScanError(exception: Throwable): ScanError? = when {
        exception is DeniedException &&
            exception.message == "Bluetooth is powered off" -> ScanError.BLUETOOTH_OFF

        else -> null
    }
}
