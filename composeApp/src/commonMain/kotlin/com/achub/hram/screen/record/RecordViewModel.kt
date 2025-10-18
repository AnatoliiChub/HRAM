package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.launchIn
import com.achub.hram.requestBleBefore
import com.achub.hram.stateInExt
import com.achub.hram.tracking.HrTracker
import com.achub.hram.tracking.SCAN_DURATION
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
    val hrTracker: HrTracker,
    @InjectedParam val permissionController: PermissionsController
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private val isBluetoothOn = MutableStateFlow(false)
    private var bleStateJob: Job? = null

    init {
        bleStateJob = bleConnectionRepo.isBluetoothOn
            .onEach { isBluetoothOn.value = it }
            .launchIn(viewModelScope, Dispatchers.Default)
    }

    fun toggleRecording() = _uiState.toggleRecordingState()
    fun stopRecording() = _uiState.stop()
    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
    fun toggleLocationTracking() = _uiState.toggleGpsTracking()
    fun cancelScanning() = hrTracker.cancelScanning()
    fun clearRequestBluetooth() = _uiState.update { it.copy(requestBluetooth = false) }
    fun openSettings() = permissionController.openAppSettings()
    fun toggleHRTracking() {
        if (_uiState.value.trackingStatus.trackHR.not()) {
            requestScanning()
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                bleConnectionRepo.disconnect()
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
        hrTracker.scan(
            onInit = { _uiState.update { it.chooseHrDeviceDialog(SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)) } },
            onUpdate = { devices -> _uiState.updateHrDeviceDialogIfExists { it.copy(scannedDevices = devices) } },
            onComplete = { _uiState.updateHrDeviceDialogIfExists { it.copy(isLoading = it.isDeviceConfirmed) } }
        )
    }

    fun onHrDeviceSelected(device: BleDevice) {
        hrTracker.listen(
            device,
            onInitConnection = _uiState::updateHrDeviceDialogConnecting,
            onConnected = _uiState::deviceConnectedDialog,
            onNewIndications = _uiState::indications
        )
    }

    override fun onCleared() {
        super.onCleared()
        cancelBleStateObservation()
        hrTracker.release()
    }

    fun cancelBleStateObservation() {
        bleStateJob?.cancel()
        bleStateJob = null
    }
}
