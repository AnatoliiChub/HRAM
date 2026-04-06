package com.achub.hram.tracking

import com.achub.hram.Logger
import com.achub.hram.data.BleDeviceRepository
import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.di.WorkerThread
import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.BleState
import com.achub.hram.models.ConnectionResultModel
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.DeviceUnavailableException
import com.achub.hram.models.ScanError
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

private const val DISCONNECTION_DELAY = 100L
private const val TAG = "HramBleConnectionOrchestrator"

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class HramBleConnectionOrchestrator(
    @param:WorkerThread
    private val dispatcher: CoroutineDispatcher,
    private val platformStateHandler: BlePlatformStateHandler,
    private val bleStateRepo: BleStateRepo,
) : BleConnectionOrchestrator, KoinComponent {
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val cancelScanning = MutableStateFlow<Boolean>(false)
    private val bleDeviceRepository: BleDeviceRepository by inject(parameters = { parametersOf(scope) })

    override fun scan(duration: Duration) = cancelScanning.apply { tryEmit(false) }
        .let { bleDeviceRepository.scan(duration) }
        .onStart { emit(ScanResultModel.Initiated) }
        .onStart { Logger.d(TAG) { "Scan started for duration: $duration" } }
        .combine(scanningCancellationTracking()) { scanResult, _ -> scanResult }
        .onEach {
            Logger.d(TAG) { "Scan result: $it" }
            when (it) {
                is ScanResultModel.Initiated -> updateBleState(BleState.Scanning.Started)
                is ScanResultModel.Complete -> updateBleState(BleState.Scanning.Completed)
                is ScanResultModel.Error -> onScanFailed(it.error)
                is ScanResultModel.ScanUpdate -> updateBleState(BleState.Scanning.Update(it.device))
            }
        }.onCompletion { cancelScanning.tryEmit(false) }

    override fun connectAndSubscribe(device: DeviceModel) = bleDeviceRepository.connect(device)
        .onStart { cancelScanning.tryEmit(true) }
        .onStart { updateBleState(BleState.Connecting(device)) }
        .onEach { if (it is ConnectionResultModel.Error) onConnectionFailed(it.error) }
        .filter { result -> result is ConnectionResultModel.Connected }
        .map { it as ConnectionResultModel.Connected }
        .map { it.device }
        .onEach { updateBleState(BleState.Connected(it)) }
        .flatMapLatest { bleDeviceRepository.listen() }

    override fun cancelScanning() {
        cancelScanning.tryEmit(true)
        scope.launch { updateBleState(BleState.Scanning.Completed) }
    }

    override suspend fun disconnect() {
        bleDeviceRepository.disconnect()
        delay(DISCONNECTION_DELAY)
        updateBleState(BleState.Disconnected)
    }

    override fun observeBleState() = bleStateRepo.listen()

    override fun reportNotification(notification: BleNotificationModel, device: DeviceModel) {
        scope.launch { updateBleState(BleState.NotificationUpdate(notification, device)) }
    }

    private fun scanningCancellationTracking() = cancelScanning.onStart { emit(false) }.distinctUntilChanged()
        .onEach { if (it) throw ScanCancelledException() }

    private suspend fun onScanFailed(exception: Throwable) {
        Logger.e(TAG) { "Scan failed: $exception" }
        val error = platformStateHandler.mapScanError(exception)
            ?: when (exception) {
                is DeviceUnavailableException -> ScanError.BLUETOOTH_OFF
                else -> ScanError.NO_BLE_PERMISSIONS
            }
        updateBleState(BleState.Scanning.Error(error, now().toEpochMilliseconds()))
    }

    private fun onConnectionFailed(exception: Throwable? = null) = scope.launch {
        Logger.e(TAG) { "Connection failed: $exception" }
        disconnect()
    }

    private suspend fun updateBleState(state: BleState) {
        bleStateRepo.update(state)
    }
}
