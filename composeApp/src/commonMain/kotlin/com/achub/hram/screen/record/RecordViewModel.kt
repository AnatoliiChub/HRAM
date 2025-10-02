package com.achub.hram.screen.record

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.achub.hram.data.BleRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.TrackingIndications
import com.achub.hram.data.model.TrackingStatus
import com.achub.hram.stateInExt
import com.achub.hram.view.RecordingState
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration

private const val SCAN_DURATION = 5_000L
class RecordViewModel(val bleRepo: BleRepo, val permissionController: PermissionsController) : ScreenModel {

    var scanJob: Job? = null
    private val _uiState = MutableStateFlow(
        RecordScreenState(
            indications = TrackingIndications(
                heartRate = 83,
                distance = 1.2f,
                duration = "00:12:34",
            ),
            trackingStatus = TrackingStatus(
                trackHR = false,
                trackGps = false,
                hrDevice = null
            ),
            recordingState = RecordingState.Init
        )
    )
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())

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
            screenModelScope.launch {
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
        _uiState.update {
            it.copy(
                dialog = RecordScreenDialog.ChooseHRDevice(isLoading = true, scannedDevices = emptyList())
            )
        }
        screenModelScope.launch {
            scanJob = bleRepo.scanHrDevices()
                .flowOn(Dispatchers.IO)
                .onCompletion {
                    val dialog = _uiState.value.dialog
                    if (dialog is RecordScreenDialog.ChooseHRDevice) {
                        _uiState.update { it.copy(dialog = dialog.copy(isLoading = false)) }
                    }
                }.onEach { device ->
                    if (_uiState.value.dialog is RecordScreenDialog.ChooseHRDevice) {
                        val scannedDevices = (_uiState.value.dialog as RecordScreenDialog.ChooseHRDevice).scannedDevices
                        if (scannedDevices.contains(device).not()) {
                            _uiState.update {
                                val newList = scannedDevices + device
                                it.copy(
                                    dialog = RecordScreenDialog.ChooseHRDevice(
                                        isLoading = true,
                                        scannedDevices = newList
                                    )
                                )
                            }
                        }
                    }
                }.catch { Napier.d { "Error: $it" } }
                .launchIn(screenModelScope)
            delay(SCAN_DURATION)
            cancelScanning()
        }
    }

    fun toggleLocationTracking() =
        _uiState.update { it.copy(trackingStatus = it.trackingStatus.copy(trackGps = it.trackingStatus.trackGps.not())) }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }


    fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (true) {
            emit(Unit)
            delay(period)
        }
    }
}
