package com.achub.hram.screen.record

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.TrackingIndications
import com.achub.hram.data.model.TrackingStatus
import com.achub.hram.view.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration

data class RecordScreenState(
    val indications: TrackingIndications = TrackingIndications(),
    val trackingStatus: TrackingStatus = TrackingStatus(),
    val recordingState: RecordingState = RecordingState.Init,
    val requestBluetooth: Boolean = false,
    val dialog: RecordScreenDialog? = null,
)

sealed class RecordScreenDialog {
    data class ChooseHRDevice(
        val isLoading: Boolean = false,
        val scannedDevices: List<BleDevice> = emptyList(),
        val loadingDuration: Duration,
        val isDeviceConfirmed: Boolean = false
    ) : RecordScreenDialog()

    data class DeviceConnectedDialog(val name: String, val manufacturer: String) : RecordScreenDialog()

    data object OpenSettingsDialog: RecordScreenDialog()
}


fun MutableStateFlow<RecordScreenState>.updateHrDeviceDialogIfExists(
    updatedDialog: (RecordScreenDialog.ChooseHRDevice) -> RecordScreenDialog.ChooseHRDevice
) = (value.dialog as? RecordScreenDialog.ChooseHRDevice)?.let {
    update { state -> state.copy(dialog = updatedDialog(it)) }
}
