package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.BleRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.stateInExt
import com.achub.hram.view.RecordingState
import com.achub.hram.view.RecordingState.Paused
import com.achub.hram.view.RecordingState.Recording
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SCAN_DURATION = 5_000L

class RecordViewModel(val bleRepo: BleRepo, val permissionController: PermissionsController) : ViewModel() {

    var scanJob: Job? = null
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())

    fun onPlay() = _uiState.update {
        it.copy(recordingState = if (it.recordingState.isRecording()) Paused else Recording)
    }

    fun onStop() = _uiState.update { it.copy(recordingState = RecordingState.Init) }
    fun toggleHRTracking() {
        val trackHR = _uiState.value.trackingStatus.trackHR
        if (trackHR.not()) {
            requestScanning()
        } else {
            _uiState.update {
                it.copy(trackingStatus = it.trackingStatus.copy(trackHR = trackHR.not(), hrDevice = null))
            }
        }
    }

    fun onDeviceSelected(deviceId: BleDevice) {
        cancelScanning()
        //TODO IMPLEMENT CONNECTING TO DEVICE before set the value
        _uiState.update {
            it.copy(
                trackingStatus = it.trackingStatus.copy(trackHR = true, hrDevice = deviceId),
                dialog = null
            )
        }
    }

    fun cancelScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    fun requestScanning() {
        if (false) {
            //TODO Check if Bluetooth is enabled, if not - request to enable it
        } else {
            viewModelScope.launch {
                try {
                    permissionController.providePermission(Permission.BLUETOOTH_SCAN)
                    permissionController.providePermission(Permission.BLUETOOTH_CONNECT)
                    scan()
                } catch (e: Exception) {
                    //TODO HANDLE DeniedAlwaysException
                    Napier.d { "Permission not granted: $e" }
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun scan() {
        cancelScanning()
        val scannedDevices = mutableListOf<BleDevice>()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                dialog = RecordScreenDialog.ChooseHRDevice(
                    isLoading = true,
                    loadingDuration = SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)
                )
            )
            scanJob = bleRepo.scanHrDevices()
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .onCompletion { _uiState.updateHrDeviceIfExists { it.copy(isLoading = false) } }
                .onEach { device ->
                    if (scannedDevices.contains(device).not()) {
                        scannedDevices.add(device)
                        _uiState.updateHrDeviceIfExists { it.copy(scannedDevices = scannedDevices) }
                    }
                }.catch { Napier.d { "Error: $it" } }
                .launchIn(viewModelScope)
            delay(SCAN_DURATION)
            cancelScanning()
        }
    }

    fun toggleLocationTracking() =
        _uiState.update { it.copy(trackingStatus = it.trackingStatus.copy(trackGps = it.trackingStatus.trackGps.not())) }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
}
