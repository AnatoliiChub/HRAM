@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.SCAN_DURATION
import com.achub.hram.cancelAndClear
import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.BleIndication
import com.achub.hram.launchIn
import com.achub.hram.requestBleBefore
import com.achub.hram.stateInExt
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.utils.ActivityNameValidation
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

class RecordViewModel(
    val trackingManager: HramActivityTrackingManager,
    val activityNameValidation: ActivityNameValidation,
    @InjectedParam val permissionController: PermissionsController
) : ViewModel(), KoinComponent {
    val bleConnectionRepo: BleConnectionRepo by inject(parameters = { parametersOf(viewModelScope) })
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private val isBluetoothOn = MutableStateFlow(false)
    private var jobs = mutableListOf<Job>()

    init {
        bleConnectionRepo.isBluetoothOn
            .onEach { isBluetoothOn.value = it }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
            .let { jobs.add(it) }
        trackingManager.bleIndication.receiveAsFlow().onStart { emit(BleIndication.Empty) }
            .onEach(_uiState::indications)
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
            .let { jobs.add(it) }
    }

    fun toggleRecording() {
        _uiState.toggleRecordingState()
        if (_uiState.isRecording) trackingManager.startTracking() else trackingManager.pauseTracking()
    }

    fun stopRecording(name: String?) =
        _uiState.stop().also { trackingManager.finishTracking(name) }.also { dismissDialog() }

    fun showNameActivityDialog() =
        _uiState.update { it.copy(dialog = RecordScreenDialog.NameActivity(activityName = "")) }

    fun onActivityNameChanged(name: String) = _uiState.update { state ->
        val currentDialog = state.dialog as? RecordScreenDialog.NameActivity
        val error = activityNameValidation(name)
        currentDialog?.let { state.copy(dialog = currentDialog.copy(activityName = name, error = error)) } ?: state
    }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
    fun cancelScanning() = trackingManager.cancelScanning()
    fun clearRequestBluetooth() = _uiState.update { it.copy(requestBluetooth = false) }
    fun openSettings() = permissionController.openAppSettings()
    fun toggleHRTracking() {
        if (_uiState.value.trackingStatus.trackHR.not()) {
            requestScanning()
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                trackingManager.disconnect()
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
