package com.achub.hram.ext

import com.achub.hram.Logger
import com.achub.hram.models.DeviceUnavailableException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN

private const val BLE_OFF_MESSAGE = "Bluetooth is powered off"

/**
 * moko-permissions-backed [BlePermissionController] shared by Android and iOS.
 */
class MokoBlePermissionController(
    private val controller: PermissionsController,
) : BlePermissionController {
    @Suppress("detekt:TooGenericExceptionCaught")
    override suspend fun requestBleBefore(action: () -> Unit, onFailure: () -> Unit) {
        try {
            controller.providePermission(Permission.BLUETOOTH_SCAN)
            controller.providePermission(Permission.BLUETOOTH_CONNECT)
            action()
        } catch (ex: Exception) {
            Logger.e("BlePermissionController") { "requestBlePermissionBeforeAction Error: $ex" }
            val bleOff = (ex is DeniedException && ex.message == BLE_OFF_MESSAGE) ||
                (ex is DeviceUnavailableException)
            if (!bleOff) onFailure()
        }
    }

    override fun openAppSettings() = controller.openAppSettings()
}


