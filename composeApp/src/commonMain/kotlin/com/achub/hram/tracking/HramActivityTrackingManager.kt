package com.achub.hram.tracking

import com.achub.hram.data.models.BleState
import com.achub.hram.data.models.ScanError
import com.achub.hram.data.repo.DeviceDataSource
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.data.repo.state.BleStateRepo
import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.di.WorkerThread
import com.achub.hram.domain.model.ACTIVE_ACTIVITY
import com.achub.hram.domain.model.BleNotificationModel
import com.achub.hram.domain.model.ConnectionResultModel
import com.achub.hram.domain.model.DeviceModel
import com.achub.hram.domain.model.DeviceUnavailableException
import com.achub.hram.domain.model.HeartRateRecord
import com.achub.hram.domain.model.ScanResultModel
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.createActivity
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.achub.hram.ext.tickerFlow
import dev.icerock.moko.permissions.DeniedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
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
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

private const val DISCONNECTION_DELAY = 100L
private const val TAG = "HramActivityTrackingManager"

@OptIn(
    FlowPreview::class,
    ExperimentalUuidApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalTime::class,
    ExperimentalAtomicApi::class
)
class HramActivityTrackingManager(
    @param:WorkerThread
    private val dispatcher: CoroutineDispatcher
) : ActivityTrackingManager, KoinComponent {
    private val stopWatch: StopWatch by inject()
    private val deviceDataSource: DeviceDataSource by inject(parameters = { parametersOf(scope) })
    private val hrActivityRepo: HrActivityRepo by inject()
    private val trackingStateRepo: TrackingStateRepo by inject()
    private val bleStateRepo: BleStateRepo by inject()
    private var scope = CoroutineScope(dispatcher + SupervisorJob())
    private val cancelScanning = MutableStateFlow<Boolean>(false)
    private var jobs = mutableListOf<Job>()

    private var currentActId: String? = null

    override fun startTracking() {
        scope.launch(dispatcher) {
            if (currentActId == null) {
                val currentTime = now().epochSeconds
                val activity = createActivity(ACTIVE_ACTIVITY, currentTime)
                currentActId = activity.id
                hrActivityRepo.insert(activity)
            }
            trackingStateRepo.update(TrackingStateStage.ACTIVE_TRACKING_STATE)
            stopWatch.start()
        }.let { jobs.add(it) }
    }

    override fun pauseTracking() {
        scope.launch { trackingStateRepo.update(TrackingStateStage.PAUSED_TRACKING_STATE) }
        stopWatch.pause()
    }

    override fun finishTracking(name: String?) {
        scope.launch(dispatcher) {
            trackingStateRepo.update(TrackingStateStage.TRACKING_INIT_STATE)
            val duration = stopWatch.elapsedTime()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override fun scan(duration: Duration) = cancelScanning.apply { tryEmit(false) }
        .let { deviceDataSource.scan(duration) }
        .onStart { emit(ScanResultModel.Initiated) }
        .onStart { logger(TAG) { "Scan started for duration: $duration" } }
        .combine(scanningCancellationTracking()) { scanResult, _ -> scanResult }
        .onEach {
            logger(TAG) { "Scan result: $it" }
            when (it) {
                is ScanResultModel.Initiated -> updateBleState(BleState.Scanning.Started)
                is ScanResultModel.Complete -> updateBleState(BleState.Scanning.Completed)
                is ScanResultModel.Error -> onScanFailed(it.error)
                is ScanResultModel.ScanUpdate -> updateBleState(BleState.Scanning.Update(it.device))
            }
        }.onCompletion { cancelScanning.tryEmit(false) }

    override fun connectAndSubscribe(device: DeviceModel) = deviceDataSource.connect(device)
        .onStart { cancelScanning.tryEmit(true) }
        .onStart { updateBleState(BleState.Connecting(device)) }
        .onEach { if (it is ConnectionResultModel.Error) onConnectionFailed(it.error) }
        .filter { result -> result is ConnectionResultModel.Connected }
        .map { it as ConnectionResultModel.Connected }
        .map { it.device }
        .onEach { updateBleState(BleState.Connected(it)) }
        .flatMapLatest { listen() }
        .onEach { notification ->
            logger(TAG) { "Ble notification received: $notification" }
            val state = BleState.NotificationUpdate(notification, device)
            updateBleState(state)
        }

    private fun listen(): Flow<BleNotificationModel> = deviceDataSource.listen()
        .combine(
            tickerFlow(1.seconds).filter { isTracking() }.onStart { emit(Unit) }
        ) { notification, _ -> notification }
        .onStart { emit(BleNotificationModel.Empty) }
        .map { it.copy(elapsedTime = stopWatch.elapsedTime()) }
        .onEach { indication -> if (isTracking() && indication.isBleConnected) store(indication) }
        .catch { loggerE(TAG) { "listen error : $it" } }

    override fun releaseState() {
        scope.launch { trackingStateRepo.release() }
    }

    override suspend fun trackingState() = trackingStateRepo.get()

    override fun cancelScanning() {
        cancelScanning.tryEmit(true)
        scope.launch { updateBleState(BleState.Scanning.Completed) }
    }

    override fun observeBleState() = bleStateRepo.listen()

    override fun disconnect() {
        scope.launch(dispatcher) {
            deviceDataSource.disconnect()
            delay(DISCONNECTION_DELAY)
            updateBleState(BleState.Disconnected)
            jobs.cancelAndClear()
        }
    }

    private suspend fun store(indication: BleNotificationModel) {
        indication.hrNotification?.let { hrNotification ->
            val isContactOn = if (hrNotification.isSensorContactSupported) hrNotification.isContactOn else true
            currentActId?.let {
                val entity = HeartRateRecord(
                    activityId = it,
                    heartRate = hrNotification.hrBpm,
                    elapsedTime = indication.elapsedTime,
                    isContactOn = isContactOn,
                    batteryLevel = indication.batteryLevel,
                    timestamp = now().toEpochMilliseconds(),
                )
                hrActivityRepo.insert(entity)
            }
        }
    }

    private fun scanningCancellationTracking() = cancelScanning.onStart { emit(false) }.distinctUntilChanged()
        .onEach { if (it) throw ScanCancelledException() }

    private suspend fun isTracking() = trackingStateRepo.get() == TrackingStateStage.ACTIVE_TRACKING_STATE

    private suspend fun onScanFailed(exception: Throwable) {
        loggerE(TAG) { "Scan failed: $exception" }
        val error = when (exception) {
            is DeniedException if exception.message == "Bluetooth is powered off" -> ScanError.BLUETOOTH_OFF
            is DeviceUnavailableException -> ScanError.BLUETOOTH_OFF
            else -> ScanError.NO_BLE_PERMISSIONS
        }
        updateBleState(BleState.Scanning.Error(error, now().toEpochMilliseconds()))
    }

    private fun onConnectionFailed(exception: Throwable? = null) = scope.launch {
        loggerE(TAG) { "Connection failed: $exception" }
        disconnect()
    }

    private suspend fun updateBleState(state: BleState) {
        bleStateRepo.update(state)
    }
}

class ScanCancelledException : CancellationException("Scan cancelled by user")
