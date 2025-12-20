@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.data.models.TrackingStatus
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.requestBleBefore
import com.achub.hram.ext.stateInExt
import com.achub.hram.tracking.HramActivityTrackingManager
import com.achub.hram.utils.ActivityNameErrorMapper
import com.juul.kable.UnmetRequirementException
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
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
    @InjectedParam val permissionController: PermissionsController,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    private var jobs = mutableListOf<Job>()
    private val scanDuration = BLE_SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)

    init {
        trackingManager.bleNotification
            .onEach(_uiState::indications)
            .flowOn(dispatcher)
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
        val error = activityNameErrorMapper(name)
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
            viewModelScope.launch(dispatcher) {
                trackingManager.disconnect()
                _uiState.toggleHrTracking()
            }
        }
    }

    fun requestScanning() = viewModelScope.launch(dispatcher) {
        permissionController.requestBleBefore(
            action = ::scan,
            onFailure = _uiState::settingsDialog,
            requestTurnOnBle = _uiState::requestBluetooth
        )
    }

    private fun scan() = trackingManager.scan(
        onInit = { _uiState.update { it.chooseHrDeviceDialog(scanDuration) } },
        onUpdate = { devices -> _uiState.updateHrDeviceDialogIfExists { it.copy(scannedDevices = devices) } },
        onComplete = { _uiState.updateHrDeviceDialogIfExists { it.copy(isLoading = it.isDeviceConfirmed) } },
        onError = { if (it is UnmetRequirementException) _uiState.requestBluetooth() }
    )

    fun onHrDeviceSelected(device: BleDevice) = trackingManager.connect(
        device,
        onInitConnection = _uiState::updateHrDeviceDialogConnecting,
        onConnected = _uiState::deviceConnectedDialog,
        onError = {
            _uiState.update {
                it.copy(
                    dialog = RecordScreenDialog.ConnectionErrorDialog,
                    trackingStatus = TrackingStatus(trackHR = false, hrDevice = null)
                )
            }
            trackingManager.disconnect()
        }
    )

    override fun onCleared() {
        super.onCleared()
        jobs.cancelAndClear()
    }
}
