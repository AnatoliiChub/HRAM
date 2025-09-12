package com.achub.hram.screen.record

import com.achub.hram.view.RecordingState

data class RecordScreenState(
    val heartRate: String = "",
    val distance: String = "",
    val duration: String = "",
    val recordingState: RecordingState = RecordingState.Init
)
