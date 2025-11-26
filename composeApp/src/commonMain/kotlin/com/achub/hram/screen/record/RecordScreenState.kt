package com.achub.hram.screen.record

import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.BleIndication
import com.achub.hram.data.models.TrackingStatus
import com.achub.hram.view.section.RecordingState
import com.achub.hram.view.section.RecordingState.Paused
import com.achub.hram.view.section.RecordingState.Recording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

data class RecordScreenState(
    val bleIndication: BleIndication = BleIndication.Empty,
    val trackingStatus: TrackingStatus = TrackingStatus(),
    val recordingState: RecordingState = RecordingState.Init,
    val requestBluetooth: Boolean = false,
    val dialog: RecordScreenDialog? = null,
) {
    fun chooseHrDeviceDialog(duration: Duration) = this.copy(
        dialog = RecordScreenDialog.ChooseHRDevice(
            isLoading = true,
            loadingDuration = duration
        )
    )
}

sealed class RecordScreenDialog {
    data class ChooseHRDevice(
        val isLoading: Boolean = false,
        val scannedDevices: List<BleDevice> = emptyList(),
        val loadingDuration: Duration,
        val isDeviceConfirmed: Boolean = false
    ) : RecordScreenDialog()

    data class NameActivity(val activityName: String, val error: String? = null) : RecordScreenDialog()

    data class DeviceConnectedDialog(val bleDevice: BleDevice) : RecordScreenDialog()

    data object OpenSettingsDialog : RecordScreenDialog()
}


fun MutableStateFlow<RecordScreenState>.updateHrDeviceDialogIfExists(
    updatedDialog: (RecordScreenDialog.ChooseHRDevice) -> RecordScreenDialog.ChooseHRDevice
) = (value.dialog as? RecordScreenDialog.ChooseHRDevice)?.let {
    update { state -> state.copy(dialog = updatedDialog(it)) }
}

fun MutableStateFlow<RecordScreenState>.settingsDialog() =
    this.update { it.copy(dialog = RecordScreenDialog.OpenSettingsDialog) }

fun MutableStateFlow<RecordScreenState>.deviceConnectedDialog(bleDevice: BleDevice) =
    this.update {
        it.copy(
            trackingStatus = it.trackingStatus.copy(trackHR = true, hrDevice = bleDevice),
            dialog = RecordScreenDialog.DeviceConnectedDialog(bleDevice)
        )
    }

fun MutableStateFlow<RecordScreenState>.toggleGpsTracking() =
    this.update { it.copy(trackingStatus = it.trackingStatus.copy(trackGps = it.trackingStatus.trackGps.not())) }

fun MutableStateFlow<RecordScreenState>.toggleHrTracking() = this.update {
    it.copy(trackingStatus = it.trackingStatus.copy(trackHR = it.trackingStatus.trackHR.not(), hrDevice = null))
}

fun MutableStateFlow<RecordScreenState>.toggleRecordingState() =
    this.update { it.copy(recordingState = if (it.recordingState.isRecording()) Paused else Recording) }

fun MutableStateFlow<RecordScreenState>.stop() = this.update { it.copy(recordingState = RecordingState.Init) }

fun MutableStateFlow<RecordScreenState>.requestBluetooth() = this.update { it.copy(requestBluetooth = true) }

fun MutableStateFlow<RecordScreenState>.indications(bleIndication: BleIndication) =
    this.update { it.copy(bleIndication = bleIndication) }

fun MutableStateFlow<RecordScreenState>.updateHrDeviceDialogConnecting() =
    this.updateHrDeviceDialogIfExists { it.copy(isDeviceConfirmed = true, isLoading = true) }

val MutableStateFlow<RecordScreenState>.isRecording: Boolean get() = value.recordingState.isRecording()
