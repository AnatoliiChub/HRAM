package com.achub.hram.screen.record

import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.SCAN_DURATION_MS
import com.achub.hram.tracking.TrackingStateStage
import com.achub.hram.tracking.TrackingStateStage.ACTIVE_TRACKING_STATE
import com.achub.hram.tracking.TrackingStateStage.PAUSED_TRACKING_STATE
import com.achub.hram.tracking.TrackingStateStage.TRACKING_INIT_STATE
import com.achub.hram.view.section.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.StringResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class RecordScreenState(
    val bleNotification: BleNotificationModel = BleNotificationModel.Empty,
    val connectedDevice: DeviceModel? = null,
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
        val scannedDevices: List<DeviceModel> = emptyList(),
        val loadingDuration: Duration,
        val isDeviceConfirmed: Boolean = false
    ) : RecordScreenDialog()

    data class NameActivity(val activityName: String, val error: StringResource? = null) : RecordScreenDialog()

    data class DeviceConnectedDialog(val bleDevice: DeviceModel) : RecordScreenDialog()

    data object OpenSettingsDialog : RecordScreenDialog()

    data object ConnectionErrorDialog : RecordScreenDialog()
}

fun MutableStateFlow<RecordScreenState>.updateHrDeviceDialogIfExists(
    updatedDialog: (RecordScreenDialog.ChooseHRDevice) -> RecordScreenDialog.ChooseHRDevice
) = (value.dialog as? RecordScreenDialog.ChooseHRDevice)?.let {
    update { state -> state.copy(dialog = updatedDialog(it)) }
}

fun MutableStateFlow<RecordScreenState>.hrDeviceDialog(scanDuration: Duration) = update {
    val dialog = RecordScreenDialog.ChooseHRDevice(isLoading = true, loadingDuration = scanDuration)
    it.copy(dialog = dialog)
}

fun MutableStateFlow<RecordScreenState>.settingsDialog() =
    update { it.copy(dialog = RecordScreenDialog.OpenSettingsDialog) }

fun MutableStateFlow<RecordScreenState>.deviceConnectedDialog(bleDevice: DeviceModel) = update {
    it.copy(connectedDevice = bleDevice, dialog = RecordScreenDialog.DeviceConnectedDialog(bleDevice))
}

fun MutableStateFlow<RecordScreenState>.toggleRecordingState() = this.update {
    val state = if (it.recordingState.isRecording()) RecordingState.Paused else RecordingState.Recording
    it.copy(recordingState = state)
}

fun MutableStateFlow<RecordScreenState>.stop() = this.update { it.copy(recordingState = RecordingState.Init) }

fun MutableStateFlow<RecordScreenState>.requestBluetooth() =
    this.update { it.copy(requestBluetooth = true, dialog = null) }

fun MutableStateFlow<RecordScreenState>.indications(bleNotification: BleNotificationModel) =
    this.update { it.copy(bleNotification = bleNotification) }

fun MutableStateFlow<RecordScreenState>.connectingProgressDialog() =
    this.update {
        it.copy(
            dialog = RecordScreenDialog.ChooseHRDevice(
                isLoading = true,
                isDeviceConfirmed = true,
                loadingDuration = SCAN_DURATION_MS.milliseconds
            )
        )
    }

val MutableStateFlow<RecordScreenState>.isRecording: Boolean get() = value.recordingState.isRecording()

fun MutableStateFlow<RecordScreenState>.clearRequestBluetooth() = this.value.requestBluetooth.also {
    if (it) this.update { state -> state.copy(requestBluetooth = false) }
}

fun TrackingStateStage.toRecordingState() = when (this) {
    TRACKING_INIT_STATE -> RecordingState.Init
    ACTIVE_TRACKING_STATE -> RecordingState.Recording
    PAUSED_TRACKING_STATE -> RecordingState.Paused
}
