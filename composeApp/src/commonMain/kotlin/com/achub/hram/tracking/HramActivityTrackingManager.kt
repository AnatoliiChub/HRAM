package com.achub.hram.tracking

import com.achub.hram.ble.ConnectionResult
import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.ScanResult
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.data.db.entity.ACTIVE_ACTIVITY
import com.achub.hram.data.db.entity.HeartRateEntity
import com.achub.hram.data.models.BleState
import com.achub.hram.data.models.ScanError
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.data.repo.state.BleStateRepo
import com.achub.hram.data.repo.state.TrackingStateRepo
import com.achub.hram.di.WorkerThread
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.createActivity
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.achub.hram.ext.tickerFlow
import com.juul.kable.UnmetRequirementException
import dev.icerock.moko.permissions.DeniedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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

private const val TAG = "HramActivityTrackingManager"
private const val SCAN_DEBOUNCE_MS = 500L

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
    private val hrDeviceRepo: HrDeviceRepo by inject(parameters = { parametersOf(scope) })
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
            val duration = stopWatch.elapsedTimeSeconds()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateNameById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override fun scan(duration: Duration) = cancelScanning.apply { tryEmit(false) }
        .let { hrDeviceRepo.scan(duration) }
        .onStart { emit(ScanResult.Initiated) }
        .onStart { logger(TAG) { "Scan started for duration: $duration" } }
        .combine(scanningCancellationTracking()) { scanResult, _ -> scanResult }
        .onEach {
            logger(TAG) { "Scan result: $it" }
            when (it) {
                is ScanResult.Initiated -> updateBleState(BleState.Scanning.Started)
                is ScanResult.Complete -> updateBleState(BleState.Scanning.Completed)
                is ScanResult.Error -> onScanFailed(it.error)
                is ScanResult.ScanUpdate -> updateBleState(BleState.Scanning.Update(it.device))
            }
        }.onCompletion { cancelScanning.tryEmit(false) }

    override fun connectAndSubscribe(device: BleDevice) = hrDeviceRepo.connect(device)
        .onStart { cancelScanning.tryEmit(true) }
        .onStart { updateBleState(BleState.Connecting(device)) }
        .onEach { if (it is ConnectionResult.Error) onConnectionFailed(it.error) }
        .filter { result -> result is ConnectionResult.Connected }
        .map { it as ConnectionResult.Connected }
        .map { it.device }
        .onEach { updateBleState(BleState.Connected(it)) }
        .flatMapLatest { listen() }
        .onEach { notification ->
            logger(TAG) { "Ble notification received: $notification" }
            val state = BleState.NotificationUpdate(notification, device)
            updateBleState(state)
        }

    private fun listen(): Flow<BleNotification> = hrDeviceRepo.listen()
        .combine(
            tickerFlow(1.seconds).filter { isTracking() }.onStart { emit(Unit) }
        ) { bleNotification, _ -> bleNotification }
        .onStart { emit(BleNotification.Empty) }
        .map { it.copy(elapsedTime = stopWatch.elapsedTimeSeconds()) }
        .onEach { bleIndication -> if (isTracking() && bleIndication.isBleConnected) store(bleIndication) }
        .catch { loggerE(TAG) { "listen error : $it" } }

    override fun releaseState() {
        scope.launch {
            trackingStateRepo.release()
        }
    }

    override suspend fun trackingState() = trackingStateRepo.get()

    override fun cancelScanning() {
        cancelScanning.tryEmit(true)
        scope.launch { updateBleState(BleState.Scanning.Completed) }
    }

    override fun observeBleState() = bleStateRepo.listen()

    override fun disconnect() {
        scope.launch(dispatcher) {
            hrDeviceRepo.disconnect()
            jobs.cancelAndClear()
            scope.launch { updateBleState(BleState.Disconnected) }
        }
    }

    private suspend fun store(bleIndication: BleNotification) {
        bleIndication.hrNotification?.let { hrNotification ->
            currentActId?.let {
                val entity = HeartRateEntity(
                    activityId = it,
                    heartRate = hrNotification.hrBpm,
                    timeStamp = bleIndication.elapsedTime
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
            is UnmetRequirementException -> ScanError.BLUETOOTH_OFF
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
