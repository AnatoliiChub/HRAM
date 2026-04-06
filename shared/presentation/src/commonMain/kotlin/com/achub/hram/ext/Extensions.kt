package com.achub.hram.ext

import androidx.compose.runtime.Composable
import com.achub.hram.Logger
import com.achub.hram.model.DeviceUnavailableException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlin.math.round

private const val DECIMAL_MULTIPLIER = 100
private const val PAD_END_LENGTH = 2
private const val BLE_OFF_MESSAGE = "Bluetooth is powered off"

/**
 * round numbers to 2 decimal places and format as string
 * Temporary fix for https://youtrack.jetbrains.com/issue/KT-21644
 */
fun Float.format(): String {
    val rounded = round(this * DECIMAL_MULTIPLIER) / DECIMAL_MULTIPLIER
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val fracPart = parts.getOrElse(1) { "0" }.padEnd(PAD_END_LENGTH, '0').take(PAD_END_LENGTH)
    return "$intPart.$fracPart"
}

@Composable
fun permissionController(): PermissionsController {
    val permissionsFactory = rememberPermissionsControllerFactory()
    val controller = permissionsFactory.createPermissionsController()
    BindEffect(controller)
    return controller
}

expect fun currentThread(): String

@Suppress("detekt:TooGenericExceptionCaught")
suspend fun PermissionsController.requestBleBefore(
    action: () -> Unit,
    onFailure: () -> Unit,
) {
    try {
        providePermission(Permission.BLUETOOTH_SCAN)
        providePermission(Permission.BLUETOOTH_CONNECT)
        action()
    } catch (ex: Exception) {
        Logger.e("PermissionsController") { "requestBlePermissionBeforeAction Error : $ex" }
        val bleOff = (ex is DeniedException) && (ex.message == BLE_OFF_MESSAGE) || (ex is DeviceUnavailableException)
        if (bleOff) return else onFailure()
    }
}

