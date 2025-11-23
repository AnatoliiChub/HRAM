@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.domain.ActivityNameValidationUseCase
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.SCAN_DURATION
import com.achub.hram.cancelAndClear
import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.HrIndication
import com.achub.hram.data.models.Indications
import com.achub.hram.launchIn
import com.achub.hram.requestBleBefore
import com.achub.hram.stateInExt
import com.achub.hram.tracking.HramActivityTrackingManager
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

@KoinViewModel
class RecordViewModel(
    val bleConnectionRepo: BleConnectionRepo,
    val trackingManager: HramActivityTrackingManager,
    val activityNameValidationUseCase: ActivityNameValidationUseCase,
    @InjectedParam val permissionController: PermissionsController
) : ViewModel() {

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
        trackingManager.hrIndication.receiveAsFlow().onStart { emit(HrIndication.Empty) }
            .combine(trackingManager.elapsedTime()) { hrIndication, elapsedTime ->
                Indications(hrIndication = hrIndication, elapsedTime = elapsedTime)
            }
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
        val error = activityNameValidationUseCase(name)
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
