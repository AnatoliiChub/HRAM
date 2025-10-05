package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.data.BleConnectionRepo
import com.achub.hram.data.BleHrDataRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.launchIn
import com.achub.hram.readManufacturerName
import com.achub.hram.stateInExt
import com.achub.hram.view.RecordingState
import com.achub.hram.view.RecordingState.Paused
import com.achub.hram.view.RecordingState.Recording
import com.juul.kable.Advertisement
import com.juul.kable.ExperimentalApi
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_CONNECT
import dev.icerock.moko.permissions.bluetooth.BLUETOOTH_SCAN
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

private const val SCAN_DURATION = 5_000L

@KoinViewModel
class RecordViewModel(
    val bleConnectionRepo: BleConnectionRepo,
    val bleHrDataRepo: BleHrDataRepo,
    @InjectedParam val permissionController: PermissionsController
) :
    ViewModel() {

    var scanJob: Job? = null
    var listenJob: Job? = null
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    val advertisements: MutableList<Advertisement> = mutableListOf()
    fun onPlay() = _uiState.update {
        it.copy(recordingState = if (it.recordingState.isRecording()) Paused else Recording)
    }

    init {
        bleConnectionRepo.init()
    }

    fun onStop() = _uiState.update { it.copy(recordingState = RecordingState.Init) }
    fun toggleHRTracking() {
        val trackHR = _uiState.value.trackingStatus.trackHR
        if (trackHR.not()) {
            requestScanning()
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                bleConnectionRepo.disconnect()
                _uiState.update {
                    it.copy(trackingStatus = it.trackingStatus.copy(trackHR = trackHR.not(), hrDevice = null))
                }
            }

        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalApi::class, ExperimentalUuidApi::class)
    fun onDeviceSelected(device: BleDevice) {
        cancelScanning()
        cancelConnection()
        advertisements.firstOrNull { it.identifier.toString() == device.identifier }?.let { advertisement ->
            _uiState.updateHrDeviceDialogIfExists { it.copy(isDeviceConfirmed = true, isLoading = true) }
            listenJob = bleConnectionRepo.connectToDevice(advertisement)
                .onEach { peripheral ->
                    val manufacturer = peripheral.readManufacturerName()
                    _uiState.update {
                        it.copy(
                            trackingStatus = it.trackingStatus.copy(trackHR = true, hrDevice = device),
                            dialog = RecordScreenDialog.DeviceConnectedDialog(
                                name = peripheral.name ?: peripheral.identifier.toString(),
                                manufacturer = manufacturer
                            )
                        )
                    }
                }
                .flatMapLatest { device -> bleHrDataRepo.observeHeartRate(device) }
                .onEach { heartRate -> _uiState.update { it.copy(indications = it.indications.copy(heartRate = heartRate)) } }
                .catch { Napier.e { "Error: $it" } }
                .launchIn(viewModelScope, Dispatchers.Default)
            //TODO IMPLEMENT CONNECTING TO DEVICE before set the value

        }
    }

    fun cancelScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    fun cancelConnection() {
        listenJob?.cancel()
        listenJob = null
    }

    fun requestScanning() {
        viewModelScope.launch {
            if (bleConnectionRepo.isBluetoothOn.value.not()) {
                requestBlePermissionBeforeAction(
                    action = { _uiState.update { it.copy(requestBluetooth = true) } },
                    onFailure = { _uiState.update { it.copy(dialog = RecordScreenDialog.OpenSettingsDialog) } })
            } else {
                requestBlePermissionBeforeAction(
                    action = ::scan,
                    onFailure = { _uiState.update { it.copy(dialog = RecordScreenDialog.OpenSettingsDialog) } })
            }
        }
    }

    fun openSettings() = permissionController.openAppSettings()

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    private fun scan() {
        viewModelScope.launch(Dispatchers.Default) {
            cancelScanning()
            val scannedDevices = mutableSetOf<BleDevice>()
            _uiState.value = _uiState.value.copy(
                dialog = RecordScreenDialog.ChooseHRDevice(
                    isLoading = true,
                    loadingDuration = SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)
                )
            )
            scanJob = bleConnectionRepo.scanHrDevices()
                .onEach { advertisements.add(it) }
                .map {
                    BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString())
                }
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .onCompletion { _uiState.updateHrDeviceDialogIfExists { it.copy(isLoading = it.isDeviceConfirmed) } }
                .onEach { device ->
                    scannedDevices.add(device)
                    _uiState.updateHrDeviceDialogIfExists { it.copy(scannedDevices = scannedDevices.toList()) }
                }.catch { Napier.d { "Error: $it" } }
                .launchIn(scope = viewModelScope, context = Dispatchers.Default)
            delay(SCAN_DURATION)
            cancelScanning()
        }
    }

    fun toggleLocationTracking() =
        _uiState.update { it.copy(trackingStatus = it.trackingStatus.copy(trackGps = it.trackingStatus.trackGps.not())) }

    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }

    override fun onCleared() {
        super.onCleared()
        cancelScanning()
        cancelConnection()
        bleConnectionRepo.release()
    }

    fun clearRequestBluetooth() {
        _uiState.update { it.copy(requestBluetooth = false) }
    }

    suspend fun requestBlePermissionBeforeAction(action: () -> Unit, onFailure: () -> Unit) {
        try {
            permissionController.providePermission(Permission.BLUETOOTH_SCAN)
            permissionController.providePermission(Permission.BLUETOOTH_CONNECT)
            action()
        } catch (e: Exception) {
            onFailure()
        }
    }
}
