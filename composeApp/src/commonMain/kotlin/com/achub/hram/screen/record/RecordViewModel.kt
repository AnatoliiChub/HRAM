package com.achub.hram.screen.record

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.achub.hram.data.BleRepo
import com.achub.hram.view.RecordingState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.seconds

class RecordViewModel(val bleRepo: BleRepo) : ScreenModel {

    var scanJob: Job? = null
    private val _uiState = MutableStateFlow(
        RecordScreenState(
            heartRate = 83,
            distance = 1.2f,
            duration = "00:12:34",
            recordingState = RecordingState.Init
        )
    )

    //TODO extract extension to ext function for ScreenModel
    val uiState = _uiState.stateIn(
        scope = screenModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordScreenState()
    )

    fun onPlay() = _uiState.update {
        val recordingState = if (it.recordingState == RecordingState.Recording) {
            RecordingState.Paused
        } else {
            RecordingState.Recording
        }
        it.copy(recordingState = recordingState)
    }

    fun onStop() = _uiState.update { it.copy(recordingState = RecordingState.Init) }
    fun toggleHRTracking() {
        if (_uiState.value.trackHR.not()) {
            onRequestScanning()
        } else {
            _uiState.update { it.copy(trackHR = it.trackHR.not(), connectedDevice = null) }
        }
    }

    fun onDeviceSelected(deviceId: String) {
        cancelScanning()
        //TODO IMPLEMENT CONNECTING TO DEVICE before set the value
        _uiState.update { it.copy(trackHR = true, dialog = null, connectedDevice = deviceId) }
    }

    fun cancelScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    @OptIn(FlowPreview::class)
    fun onRequestScanning() {
        _uiState.update {
            it.copy(
                scannedDevices = emptyList(),
                dialog = RecordScreenDialog.ChooseHRDevice(isLoading = true)
            )
        }
        scanJob = bleRepo.scanHrDevices()
            .flowOn(Dispatchers.IO)
            .onCompletion {
                Napier.d { "OnCompletion" }
                if (_uiState.value.dialog is RecordScreenDialog.ChooseHRDevice) {
                    _uiState.update {
                        it.copy(
                            dialog = RecordScreenDialog.ChooseHRDevice(isLoading = false)
                        )
                    }
                }
            }.timeout(15.seconds)
            .onEach { device ->
                if (_uiState.value.scannedDevices.contains(device).not()) {
                    _uiState.update {
                        val newList = it.scannedDevices + device
                        it.copy(
                            dialog = RecordScreenDialog.ChooseHRDevice(
                                isLoading = true
                            ),
                            scannedDevices = newList
                        )
                    }
                }
            }.catch { Napier.d { "Error: $it" } }
            .launchIn(screenModelScope)
    }

    fun toggleLocationTracking() = _uiState.update { it.copy(trackLocation = it.trackLocation.not()) }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
}
