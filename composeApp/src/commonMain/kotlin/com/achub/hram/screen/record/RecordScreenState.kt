package com.achub.hram.screen.record

import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.view.section.RecordingState
import com.achub.hram.view.section.RecordingState.Paused
import com.achub.hram.view.section.RecordingState.Recording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class RecordScreenState(
    val bleNotification: BleNotification = BleNotification.Empty,
    val connectedDevice: BleDevice? = null,
    val recordingState: RecordingState = RecordingState.Init,
    val requestBluetooth: Boolean = false,
    val dialog: RecordScreenDialog? = null,
) {
    val isRecordingEnabled: Boolean
        get() = connectedDevice != null || recordingState != RecordingState.Init
}

sealed class RecordScreenDialog {
    data class ChooseHRDevice(
        val isLoading: Boolean = false,
        val scannedDevices: List<BleDevice> = emptyList(),
        val loadingDuration: Duration,
        val isDeviceConfirmed: Boolean = false
    ) : RecordScreenDialog()

    data class NameActivity(val activityName: String, val error: StringResource? = null) : RecordScreenDialog()

    data class DeviceConnectedDialog(val bleDevice: BleDevice) : RecordScreenDialog()

    data object OpenSettingsDialog : RecordScreenDialog()

    data object ConnectionErrorDialog : RecordScreenDialog()
}

fun MutableStateFlow<RecordScreenState>.updateHrDeviceDialogIfExists(
    updatedDialog: (RecordScreenDialog.ChooseHRDevice) -> RecordScreenDialog.ChooseHRDevice
) = (value.dialog as? RecordScreenDialog.ChooseHRDevice)?.let {
    update { state -> state.copy(dialog = updatedDialog(it)) }
}

fun MutableStateFlow<RecordScreenState>.hrDeviceDialog(scanDuration: Duration) =
    update {
        it.copy(
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = true,
                loadingDuration = scanDuration
            )
        )
    }

fun MutableStateFlow<RecordScreenState>.settingsDialog() =
    update { it.copy(dialog = RecordScreenDialog.OpenSettingsDialog) }

fun MutableStateFlow<RecordScreenState>.deviceConnectedDialog(bleDevice: BleDevice) = update {
    it.copy(connectedDevice = bleDevice, dialog = RecordScreenDialog.DeviceConnectedDialog(bleDevice))
}

fun MutableStateFlow<RecordScreenState>.toggleRecordingState() =
    this.update { it.copy(recordingState = if (it.recordingState.isRecording()) Paused else Recording) }

fun MutableStateFlow<RecordScreenState>.stop() = this.update { it.copy(recordingState = RecordingState.Init) }

fun MutableStateFlow<RecordScreenState>.requestBluetooth() =
    this.update { it.copy(requestBluetooth = true, dialog = null) }

fun MutableStateFlow<RecordScreenState>.indications(bleNotification: BleNotification) =
    this.update { it.copy(bleNotification = bleNotification) }

fun MutableStateFlow<RecordScreenState>.connectingProgressDialog(device: BleDevice) =
    this.update {
        it.copy(
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = true,
                isDeviceConfirmed = true,
                loadingDuration = BLE_SCAN_DURATION.milliseconds
            )
        )
    }

val MutableStateFlow<RecordScreenState>.isRecording: Boolean get() = value.recordingState.isRecording()

fun MutableStateFlow<RecordScreenState>.clearRequestBluetooth() = this.value.requestBluetooth.also {
    if (it) this.update { state -> state.copy(requestBluetooth = false) }
}
