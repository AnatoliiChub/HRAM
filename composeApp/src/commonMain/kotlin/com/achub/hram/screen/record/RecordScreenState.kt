package com.achub.hram.screen.record

import com.achub.hram.screen.base.BaseUiState
import com.achub.hram.view.RecordingState

data class RecordScreenState(
    val heartRate: Int = 122,
    val distance: Float = 1.44f,
    val duration: String = "",
    val trackHR: Boolean = false,
    val trackLocation: Boolean = false,
    val recordingState: RecordingState = RecordingState.Init
) : BaseUiState
