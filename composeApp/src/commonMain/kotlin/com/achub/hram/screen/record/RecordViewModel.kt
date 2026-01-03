@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.data.models.BleState
import com.achub.hram.data.repo.TrackingStateRepo
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.requestBleBefore
import com.achub.hram.ext.stateInExt
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.tracking.TrackingController
import com.achub.hram.utils.ActivityNameErrorMapper
import dev.icerock.moko.permissions.PermissionsController
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

class RecordViewModel(
    val trackingManager: HramActivityTrackingManager,
    val activityNameErrorMapper: ActivityNameErrorMapper,
    val dispatcher: CoroutineDispatcher,
    val trackingController: TrackingController,
    val trackingStateRepo: TrackingStateRepo,
    @InjectedParam val permissionController: PermissionsController,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private var jobs = mutableListOf<Job>()
    private val scanDuration = BLE_SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)

    init {
        trackingStateRepo.listenTrackingState().onStart { trackingStateRepo.release() }.onEach { state ->
            Napier.d { "new state : $state" }
            when (state) {
                is BleState.Scanning -> {
                    when (state) {
                        is BleState.Scanning.Started -> _uiState.update {
                            it.copy(
                                dialog = RecordScreenDialog.ChooseHRDevice(
                                    isLoading = true,
                                    loadingDuration = scanDuration
                                )
                            )
                        }

                        is BleState.Scanning.Error,
                        is BleState.Scanning.Completed -> _uiState.updateHrDeviceDialogIfExists {
                            it.copy(isLoading = false)
                        }

                        is BleState.Scanning.Update -> _uiState.updateHrDeviceDialogIfExists { dialog ->
                            if (dialog.scannedDevices.any { it.identifier == state.device.identifier }.not()) {
                                dialog.copy(scannedDevices = dialog.scannedDevices + state.device)
                            } else {
                                dialog
                            }
                        }
                    }
                }

                is BleState.Connecting -> _uiState.updateHrDeviceDialogConnecting()

                is BleState.Connected -> {
                    if (state.error != null) {
                        _uiState.update {
                            it.copy(dialog = RecordScreenDialog.ConnectionErrorDialog, connectedDevice = null)
                        }
                    } else {
                        _uiState.deviceConnectedDialog(state.bleDevice)
                    }
                }

                is BleState.NotificationUpdate -> _uiState.indications(state.bleNotification)

                is BleState.Disconnected -> {
                    _uiState.update { it.copy(connectedDevice = null) }
                }
            }
        }.flowOn(dispatcher).launchIn(viewModelScope).let { jobs.add(it) }
    }

    fun toggleRecording() {
        _uiState.toggleRecordingState()
        if (_uiState.isRecording) trackingManager.startTracking() else trackingManager.pauseTracking()
    }

    fun stopRecording(name: String?) = _uiState.stop().also { trackingManager.finishTracking(name) }.also {
        _uiState.update { it.copy(dialog = null, connectedDevice = null) }
        viewModelScope.launch(dispatcher) { trackingManager.disconnect() }
    }

    fun showNameActivityDialog() =
        _uiState.update { it.copy(dialog = RecordScreenDialog.NameActivity(activityName = "")) }

    fun onActivityNameChanged(name: String) = _uiState.update { state ->
        val currentDialog = state.dialog as? RecordScreenDialog.NameActivity
        val error = activityNameErrorMapper(name)
        currentDialog?.let { state.copy(dialog = currentDialog.copy(activityName = name, error = error)) } ?: state
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }

    fun cancelScanning() {
        // TODO cancel scanning
    }

    fun clearRequestBluetooth() = _uiState.update { it.copy(requestBluetooth = false) }

    fun openSettings() = permissionController.openAppSettings()

    fun requestScanning() = viewModelScope.launch(dispatcher) {
        permissionController.requestBleBefore(
            action = ::scan,
            onFailure = _uiState::settingsDialog,
            requestTurnOnBle = _uiState::requestBluetooth
        )
    }

    private fun scan() = trackingController.scan()

    fun onHrDeviceSelected(device: BleDevice) = trackingController.connectDevice(device)

//        trackingManager.connect(
//        device,
//        onInitConnection = _uiState::updateHrDeviceDialogConnecting,
//        onConnected = {
//        },
//        onError = {
//            _uiState.update { it.copy(dialog = RecordScreenDialog.ConnectionErrorDialog, connectedDevice = null) }
//            trackingManager.disconnect()
//        }
//    )

    override fun onCleared() {
        super.onCleared()
        jobs.cancelAndClear()
    }
}
