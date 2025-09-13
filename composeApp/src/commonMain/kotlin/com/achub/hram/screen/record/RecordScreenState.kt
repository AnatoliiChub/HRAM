package com.achub.hram.screen.record

import com.achub.hram.view.RecordingState

//TODO restructure the state, since there is too many fields
data class RecordScreenState(
    val heartRate: Int = 122,
    val distance: Float = 1.44f,
    val duration: String = "",
    val trackHR: Boolean = false,
    val trackLocation: Boolean = false,
    val recordingState: RecordingState = RecordingState.Init,
    val scannedDevices: List<String> = emptyList(),
    val connectedDevice: String? = null,
    val dialog: RecordScreenDialog? = null
)

sealed class RecordScreenDialog {
    data class ChooseHRDevice(val isLoading: Boolean = false) : RecordScreenDialog()
}
