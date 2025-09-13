package com.achub.hram.screen.record

import cafe.adriel.voyager.core.model.screenModelScope
import com.achub.hram.screen.base.BaseActionHandler
import com.achub.hram.screen.base.BaseViewModel
import com.achub.hram.view.RecordingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class RecordViewModel : BaseViewModel<RecordActionHandler, RecordScreenState>() {

    private val _uiState = MutableStateFlow(
        RecordScreenState(
            heartRate = 83,
            distance = 1.2f,
            duration = "00:12:34",
            recordingState = RecordingState.Init
        )
    )

    override var actionHandler: RecordActionHandler? = object : RecordActionHandler {
        override fun onPlay() = _uiState.update {
            val recordingState = if (it.recordingState == RecordingState.Recording) {
                RecordingState.Paused
            } else {
                RecordingState.Recording
            }
            it.copy(recordingState = recordingState)
        }
        override fun onStop() = _uiState.update { it.copy(recordingState = RecordingState.Init) }
        override fun toggleHRTracking() = _uiState.update { it.copy(trackHR = it.trackHR.not()) }
        override fun toggleLocationTracking() = _uiState.update { it.copy(trackLocation = it.trackLocation.not()) }
    }
    override val uiState = _uiState.stateIn(
        scope = screenModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordScreenState()
    )
}

interface RecordActionHandler: BaseActionHandler {
    fun onPlay()
    fun onStop()
    fun toggleHRTracking()
    fun toggleLocationTracking()
}