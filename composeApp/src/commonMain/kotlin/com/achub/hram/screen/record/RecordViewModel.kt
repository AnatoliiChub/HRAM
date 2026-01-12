@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.data.models.BleState
import com.achub.hram.data.models.ScanError
import com.achub.hram.data.repo.state.BleStateRepo
import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.requestBleBefore
import com.achub.hram.ext.stateInExt
import com.achub.hram.tracking.TrackingController
import com.achub.hram.tracking.TrackingStateStage
import com.achub.hram.tracking.TrackingStateStage.ACTIVE_TRACKING_STATE
import com.achub.hram.tracking.TrackingStateStage.PAUSED_TRACKING_STATE
import com.achub.hram.tracking.TrackingStateStage.TRACKING_INIT_STATE
import com.achub.hram.utils.ActivityNameErrorMapper
import com.achub.hram.view.section.RecordingState
import dev.icerock.moko.permissions.PermissionsController
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

class RecordViewModel(
    val activityNameErrorMapper: ActivityNameErrorMapper,
    val dispatcher: CoroutineDispatcher,
    val trackingController: TrackingController,
    val bleStateRepo: BleStateRepo,
    val trackingStateRepo: TrackingStateRepo,
    @InjectedParam val permissionController: PermissionsController,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private var jobs = mutableListOf<Job>()
    private val scanDuration = BLE_SCAN_DURATION.milliseconds

    init {
        trackingStateRepo.listen().onEach { state ->
            _uiState.update { it.copy(recordingState = state.toRecordingState()) }
        }.flowOn(dispatcher).launchIn(viewModelScope).let { jobs.add(it) }
        bleStateRepo.listen().onStart { bleStateRepo.release() }.onEach { state ->
            when (state) {
                is BleState.Scanning -> handleScanning(state)
                is BleState.Connecting -> _uiState.connectingProgressDialog(state.device)
                is BleState.Connected -> handleConnectedState(state)
                is BleState.NotificationUpdate -> handleNotificationUpdate(state)
                is BleState.Disconnected -> handleDisconnected()
            }
        }.flowOn(dispatcher).launchIn(viewModelScope).let { jobs.add(it) }
    }

    fun toggleRecording() {
        _uiState.toggleRecordingState()
        if (_uiState.isRecording) trackingController.startTracking() else trackingController.pauseTracking()
    }

    fun stopRecording(name: String?) = _uiState.stop().also {
        trackingController.finishTracking(name ?: "Temporary name")
        _uiState.update { it.copy(dialog = null, connectedDevice = null) }
    }

    fun showNameActivityDialog() =
        _uiState.update { it.copy(dialog = RecordScreenDialog.NameActivity(activityName = "")) }

    fun onActivityNameChanged(name: String) = _uiState.update { state ->
        val currentDialog = state.dialog as? RecordScreenDialog.NameActivity
        val error = activityNameErrorMapper(name)
        currentDialog?.let { state.copy(dialog = currentDialog.copy(activityName = name, error = error)) } ?: state
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }

    fun cancelScanning() = trackingController.cancelScanning()

    fun clearRequestBluetooth() = _uiState.clearRequestBluetooth()

    fun openSettings() = permissionController.openAppSettings()

    fun scan() = trackingController.scan()

    fun disconnect() = trackingController.disconnectDevice()

    fun requestScanning() = viewModelScope.launch(dispatcher) {
        permissionController.requestBleBefore(action = ::scan, onFailure = _uiState::settingsDialog)
    }

    fun onHrDeviceSelected(device: BleDevice) = trackingController.connectDevice(device)

    override fun onCleared() {
        super.onCleared()
        trackingController.clear()
        jobs.cancelAndClear()
    }

    private fun handleScanning(state: BleState.Scanning) {
        when (state) {
            is BleState.Scanning.Started -> _uiState.hrDeviceDialog(scanDuration)

            is BleState.Scanning.Error -> when (state.error) {
                ScanError.BLUETOOTH_OFF -> _uiState.requestBluetooth()
                ScanError.NO_BLE_PERMISSIONS -> _uiState.settingsDialog()
            }

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

    private fun handleNotificationUpdate(state: BleState.NotificationUpdate) {
        _uiState.indications(state.bleNotification).also {
            if (state.bleNotification.isBleConnected) _uiState.update { it.copy(connectedDevice = state.device) }
        }
    }

    private fun handleConnectedState(state: BleState.Connected) = if (state.error != null) {
        _uiState.update { it.copy(dialog = RecordScreenDialog.ConnectionErrorDialog, connectedDevice = null) }
    } else {
        _uiState.deviceConnectedDialog(state.bleDevice)
    }

    private fun handleDisconnected() {
        _uiState.update { it.copy(connectedDevice = null, bleNotification = BleNotification.Empty) }
    }
}

fun TrackingStateStage.toRecordingState() = when (this) {
    TRACKING_INIT_STATE -> RecordingState.Init
    ACTIVE_TRACKING_STATE -> RecordingState.Recording
    PAUSED_TRACKING_STATE -> RecordingState.Paused
}
