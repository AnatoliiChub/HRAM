package com.achub.hram.screen.record

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.achub.hram.view.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RecordViewModel : ScreenModel {

    private val _uiState = MutableStateFlow(
        RecordScreenState("83", "1.2 km", "00:12:34", RecordingState.Init)
    )

    val uiState = _uiState.stateIn(
        scope = screenModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordScreenState()
    )

    fun onPlay() =
        _uiState.update {
            val recordingState = if (_uiState.value.recordingState == RecordingState.Recording) {
                RecordingState.Paused
            } else {
                RecordingState.Recording
            }
            _uiState.value.copy(recordingState = recordingState)
        }

    fun onStop() =
        _uiState.update { _uiState.value.copy(recordingState = RecordingState.Init) }

}
