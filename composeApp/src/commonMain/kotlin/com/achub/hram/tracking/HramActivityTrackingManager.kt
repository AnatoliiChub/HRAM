package com.achub.hram.tracking

import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.data.db.entity.ACTIVE_ACTIVITY
import com.achub.hram.data.db.entity.HeartRateEntity
import com.achub.hram.data.repo.HrActivityRepo
import com.achub.hram.di.WorkerThread
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.createActivity
import com.achub.hram.ext.loggerE
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.time.Clock.System.now
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

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
    private val hrDeviceRepo: HrDeviceRepo by inject(parameters = { parametersOf(scope) })
    private val hrActivityRepo: HrActivityRepo by inject()

    private var scope = CoroutineScope(dispatcher + SupervisorJob())
    private val trackingState = AtomicInt(TrackingStateStage.TRACKING_INIT_STATE.ordinal)
    private var jobs = mutableListOf<Job>()
    private val isRecording get() = trackingState.load() == TrackingStateStage.ACTIVE_TRACKING_STATE.ordinal
    private var currentActId: String? = null

    override fun startTracking() {
        scope.launch(dispatcher) {
            if (currentActId == null) {
                val currentTime = now().epochSeconds
                val activity = createActivity(ACTIVE_ACTIVITY, currentTime)
                currentActId = activity.id
                hrActivityRepo.insert(activity)
            }
            trackingState.update { TrackingStateStage.ACTIVE_TRACKING_STATE.ordinal }
            stopWatch.start()
        }.let { jobs.add(it) }
    }

    override fun pauseTracking() {
        trackingState.update { TrackingStateStage.PAUSED_TRACKING_STATE.ordinal }
        stopWatch.pause()
    }

    override fun finishTracking(name: String?) {
        scope.launch(dispatcher) {
            trackingState.update { TrackingStateStage.TRACKING_INIT_STATE.ordinal }
            val duration = stopWatch.elapsedTimeSeconds()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateNameById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override fun scan(duration: Duration) = hrDeviceRepo.scan(duration)

    override fun connect(device: BleDevice) = hrDeviceRepo.connect(device)

    override fun listen(): Flow<BleNotification> = hrDeviceRepo.listen()
        .onStart { emit(BleNotification.Empty) }
        .map { it.copy(elapsedTime = stopWatch.elapsedTimeSeconds()) }
        .onEach { bleIndication -> if (isRecording && bleIndication.isBleConnected) store(bleIndication) }
        .catch { loggerE(TAG) { "listen error : $it" } }

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

    override fun disconnect() {
        scope.launch(dispatcher) {
            hrDeviceRepo.disconnect()
            jobs.cancelAndClear()
        }
    }
}
