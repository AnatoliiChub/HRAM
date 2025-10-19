package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.SCAN_DURATION
import com.achub.hram.cancelAndClear
import com.achub.hram.data.model.BleDevice
import com.achub.hram.launchIn
import com.achub.hram.requestBleBefore
import com.achub.hram.stateInExt
import com.achub.hram.tracking.HramActivityTrackingService
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@KoinViewModel
class RecordViewModel(
    val bleConnectionRepo: BleConnectionRepo,
    val trackingManager: HramActivityTrackingService,
    @InjectedParam val permissionController: PermissionsController
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private val isBluetoothOn = MutableStateFlow(false)
    private var jobs = mutableListOf<Job>()

    init {
        bleConnectionRepo.isBluetoothOn
            .onEach { isBluetoothOn.value = it }
            .launchIn(viewModelScope, Dispatchers.Default)
            .let { jobs.add(it) }
        trackingManager.listen()
            .onEach(_uiState::indications)
            .launchIn(viewModelScope, Dispatchers.Default)
            .let { jobs.add(it) }
    }

    fun toggleRecording() = _uiState.toggleRecordingState().also {
        if (_uiState.isRecording) trackingManager.startTracking() else trackingManager.pauseTracking()
    }

    fun stopRecording() = _uiState.stop().also {
        trackingManager.finishTracking()
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
    fun toggleLocationTracking() = _uiState.toggleGpsTracking()
    fun cancelScanning() = trackingManager.cancelScanning()
    fun clearRequestBluetooth() = _uiState.update { it.copy(requestBluetooth = false) }
    fun openSettings() = permissionController.openAppSettings()
    fun toggleHRTracking() {
        if (_uiState.value.trackingStatus.trackHR.not()) {
            requestScanning()
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                trackingManager.disconect()
                _uiState.toggleHrTracking()
            }
        }
    }

    fun requestScanning() {
        viewModelScope.launch(Dispatchers.Default) {
            val action = if (isBluetoothOn.value.not()) _uiState::requestBluetooth else ::scan
            val onFailure = if (isBluetoothOn.value.not()) null else _uiState::settingsDialog
            permissionController.requestBleBefore(action = action, onFailure = { onFailure?.invoke() })
        }
    }

    private fun scan() {
        trackingManager.scan(
            onInit = { _uiState.update { it.chooseHrDeviceDialog(SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)) } },
            onUpdate = { devices -> _uiState.updateHrDeviceDialogIfExists { it.copy(scannedDevices = devices) } },
            onComplete = { _uiState.updateHrDeviceDialogIfExists { it.copy(isLoading = it.isDeviceConfirmed) } }
        )
    }

    fun onHrDeviceSelected(device: BleDevice) {
        trackingManager.connect(
            device,
            onInitConnection = _uiState::updateHrDeviceDialogConnecting,
            onConnected = _uiState::deviceConnectedDialog,
        )
    }

    override fun onCleared() {
        super.onCleared()
        jobs.cancelAndClear()
    }
}
