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
    val dialog: RecordScreenDialog? = null
)

sealed class RecordScreenDialog {
    data class ChooseHRDevice(
        val isLoading: Boolean = false,
        val scannedDevices: List<BleDevice> = emptyList(),
        val loadingDuration: Duration
    ) : RecordScreenDialog()
}

fun MutableStateFlow<RecordScreenState>.updateHrDeviceIfExists(
    updatedDialog: (RecordScreenDialog.ChooseHRDevice) -> RecordScreenDialog.ChooseHRDevice
) = (value.dialog as? RecordScreenDialog.ChooseHRDevice)?.let {
    update { state -> state.copy(dialog = updatedDialog(it)) }
}

