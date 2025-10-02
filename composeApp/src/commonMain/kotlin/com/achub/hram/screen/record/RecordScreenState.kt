package com.achub.hram.screen.record

import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.TrackingIndications
import com.achub.hram.data.model.TrackingStatus
import com.achub.hram.view.RecordingState

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
    ) : RecordScreenDialog()
}
