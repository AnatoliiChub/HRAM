package com.achub.hram.screen.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.BleDataRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrNotifications
import com.achub.hram.launchIn
import com.achub.hram.logger
import com.achub.hram.loggerE
import com.achub.hram.requestBleBefore
import com.achub.hram.stateInExt
import com.juul.kable.Advertisement
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.State
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import kotlin.time.Clock.System.now
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

private const val SCAN_DURATION = 5_000L
private val TAG = "RecordViewModel"

@KoinViewModel
class RecordViewModel(
    val bleConnectionRepo: BleConnectionRepo,
    val bleDataRepo: BleDataRepo,
    @InjectedParam val permissionController: PermissionsController
) : ViewModel() {

    var scanJob: Job? = null
    val listenJob = mutableListOf<Job>()
    var bleStateJob: Job? = null
    private val _uiState = MutableStateFlow(RecordScreenState())
    val uiState = _uiState.stateInExt(initialValue = RecordScreenState())
    val advertisements: MutableList<Advertisement> = mutableListOf()
    val isBluetoothOn = MutableStateFlow(false)

    init {
        bleStateJob = bleConnectionRepo.isBluetoothOn
            .onEach { isBluetoothOn.value = it }
            .launchIn(viewModelScope, Dispatchers.Default)
    }

    fun onPlay() = _uiState.toggleRecordingState()
    fun onStop() = _uiState.stop()
    fun dismissDialog() = _uiState.update { it.copy(dialog = null) }
    fun toggleLocationTracking() = _uiState.toggleGpsTracking()
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

    @OptIn(
        ExperimentalCoroutinesApi::class,
        ExperimentalApi::class,
        ExperimentalUuidApi::class,
        ExperimentalTime::class
    )
    fun onDeviceSelected(device: BleDevice) {
        cancelScanning()
        cancelConnection()
        advertisements.firstOrNull { it.identifier.toString() == device.identifier }?.let { advertisement ->
            _uiState.updateHrDeviceDialogIfExists { it.copy(isDeviceConfirmed = true, isLoading = true) }
            bleConnectionRepo.connectToDevice(advertisement.identifier)
                .withIndex()
                .onEach { (index, device) ->
                    if (index > 0) return@onEach
                    _uiState.update { it.deviceConnectedDialog(device) }
                }.catch { loggerE(TAG) { "Error while connecting to device: $it" } }
                .onCompletion { logger(TAG) { "ConnectToDevice job completed" } }
                .launchIn(viewModelScope, Dispatchers.Default)
                .let { listenJob.add(it) }
            bleConnectionRepo.onConnected
                .flatMapLatest { device -> hrIndicationCombiner(device) }
                .onEach { _uiState.indications(it) }
                .catch { loggerE(TAG) { "Error: $it" } }
                .launchIn(viewModelScope, Dispatchers.Default)
                .let { listenJob.add(it) }
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun hrIndicationCombiner(device: Peripheral): Flow<HrNotifications> = combine(
        bleDataRepo.observeHeartRate(device),
        bleDataRepo.observeBatteryLevel(device),
        device.state
    ) { hrRate, battery, state ->
        if (state !is State.Connected) {
            HrNotifications.Empty
        } else {
            HrNotifications(hrRate, battery, now().toEpochMilliseconds())
        }
    }

    fun requestScanning() {
        viewModelScope.launch(Dispatchers.Default) {
            val action = if (isBluetoothOn.value.not()) _uiState::requestBluetooth else ::scan
            permissionController.requestBleBefore(action = action, onFailure = { _uiState.settingsDialog() })
        }
    }

    fun openSettings() = permissionController.openAppSettings()

    @OptIn(FlowPreview::class, ExperimentalUuidApi::class)
    private fun scan() {
        viewModelScope.launch(Dispatchers.Default) {
            cancelScanning()
            val scannedDevices = mutableSetOf<BleDevice>()
            _uiState.update { it.chooseHrDeviceDialog(SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)) }
            scanJob = bleConnectionRepo.scanHrDevices()
                .onEach { advertisements.add(it) }
                .map { BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }
                .flowOn(Dispatchers.IO)
                .distinctUntilChanged()
                .onCompletion { _uiState.updateHrDeviceDialogIfExists { it.copy(isLoading = it.isDeviceConfirmed) } }
                .onEach { device ->
                    scannedDevices.add(device)
                    _uiState.updateHrDeviceDialogIfExists { it.copy(scannedDevices = scannedDevices.toList()) }
                }.catch { loggerE(TAG) { "Error: $it" } }
                .launchIn(scope = viewModelScope, context = Dispatchers.Default)
            delay(SCAN_DURATION)
            cancelScanning()
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelScanning()
        cancelConnection()
        cancelBleStateObservation()
    }

    fun clearRequestBluetooth() = _uiState.update { it.copy(requestBluetooth = false) }

    fun cancelScanning() {
        scanJob?.cancel()
        scanJob = null
    }

    fun cancelConnection() {
        listenJob.forEach { it.cancel() }
        listenJob.clear()
    }

    fun cancelBleStateObservation() {
        bleStateJob?.cancel()
        bleStateJob = null
    }
}
