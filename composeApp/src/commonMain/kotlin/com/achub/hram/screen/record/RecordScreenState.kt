package com.achub.hram.screen.record

import com.achub.hram.view.RecordingState

data class RecordScreenState(
    val indications: RecordScreenIndications = RecordScreenIndications(),
    val checkboxes: RecordScreenCheckboxes = RecordScreenCheckboxes(),
    val recordingState: RecordingState = RecordingState.Init,
    val dialog: RecordScreenDialog? = null
)
 data class RecordScreenIndications(
    val heartRate: Int = 122,
    val distance: Float = 1.44f,
    val duration: String = "",
 )

data class RecordScreenCheckboxes(
    val trackHR: Boolean = false,
    val trackGps: Boolean = false,
    val hrDevice: String? = null
)

sealed class RecordScreenDialog {
    data class ChooseHRDevice(
        val isLoading: Boolean = false,
        val scannedDevices: List<String> = emptyList()
    ) : RecordScreenDialog()
}
