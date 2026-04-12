package com.achub.hram.ext

import androidx.compose.runtime.Composable
import com.achub.hram.view.dialogs.InfoDialog
import hram.composeapp.generated.resources.Res
import hram.composeapp.generated.resources.dialog_open_setting_button_text
import hram.composeapp.generated.resources.dialog_open_setting_message
import hram.composeapp.generated.resources.dialog_open_setting_title
import org.jetbrains.compose.resources.stringResource
import java.awt.Desktop
import java.net.URI

private const val BLUETOOTH_PREFERENCE_URL = "x-apple.systempreferences:com.apple.Bluetooth"

private fun isMacOs() = System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true

@Suppress("ComposableNaming")
@Composable
actual fun requestBluetooth(onRequested: () -> Unit) {
    InfoDialog(
        title = Res.string.dialog_open_setting_title,
        message = stringResource(Res.string.dialog_open_setting_message),
        buttonText = Res.string.dialog_open_setting_button_text,
        onButtonClick = {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // TODO IMPLEMENT FOR OTHER OSes
                if (isMacOs()) Desktop.getDesktop().browse(URI(BLUETOOTH_PREFERENCE_URL))
            }
            onRequested()
        },
        onDismiss = onRequested
    )
}

/** Desktop has no OS-level BLE permission flow — return a no-op controller. */
@Composable
actual fun permissionController(): BlePermissionController = NoOpBlePermissionController

private object NoOpBlePermissionController : BlePermissionController {
    override suspend fun requestBleBefore(action: () -> Unit, onFailure: () -> Unit) = action()

    override fun openAppSettings() = Unit
}
