package com.achub.hram.tracking

import com.achub.hram.models.ScanError
import dev.icerock.moko.permissions.DeniedException

/**
 * moko-permissions-aware [BlePlatformStateHandler] shared by Android and iOS.
 * Recognises [DeniedException] with the "Bluetooth is powered off" message that moko
 * raises when the system Bluetooth adapter is disabled.
 */
class MokoBlePlatformStateHandler : BlePlatformStateHandler {
    override fun mapScanError(exception: Throwable): ScanError? = when {
        exception is DeniedException &&
            exception.message == "Bluetooth is powered off" -> ScanError.BLUETOOTH_OFF

        else -> null
    }
}

