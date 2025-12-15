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
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
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
    override val bleNotification = MutableStateFlow(BleNotification.Empty)
    private val stopWatch: StopWatch by inject()
    private val hrDeviceRepo: HrDeviceRepo by inject(parameters = { parametersOf(scope) })
    private val hrActivityRepo: HrActivityRepo by inject()

    private var scope = CoroutineScope(dispatcher + SupervisorJob())
    private val trackingState = AtomicInt(TRACKING_INIT_STATE)
    private var jobs = mutableListOf<Job>()
    private val isRecording get() = trackingState.load() == ACTIVE_TRACKING_STATE
    private var currentActId: String? = null

    override fun startTracking() {
        scope.launch(dispatcher) {
            if (currentActId == null) {
                val currentTime = now().epochSeconds
                val activity = createActivity(ACTIVE_ACTIVITY, currentTime)
                currentActId = activity.id
                hrActivityRepo.insert(activity)
            }
            trackingState.update { ACTIVE_TRACKING_STATE }
            stopWatch.start()
        }.let { jobs.add(it) }
    }

    override fun pauseTracking() {
        trackingState.update { PAUSED_TRACKING_STATE }
        stopWatch.pause()
    }

    override fun finishTracking(name: String?) {
        scope.launch(dispatcher) {
            trackingState.update { TRACKING_INIT_STATE }
            val duration = stopWatch.elapsedTimeSeconds()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateNameById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override fun scan(
        onInit: () -> Unit,
        onUpdate: (List<BleDevice>) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) = hrDeviceRepo.scan(onInit, onUpdate, onComplete, onError)

    override fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit
    ) = hrDeviceRepo.connect(device, onInitConnection, onConnected).also {
        listen().flowOn(dispatcher).launchIn(scope).let { jobs.add(it) }
    }

    private fun listen() = hrDeviceRepo.listen().onStart { emit(BleNotification.Empty) }
        .map { it.copy(elapsedTime = stopWatch.elapsedTimeSeconds()) }
        .onEach { bleNotification.value = it }
        .filter { isRecording && it.isBleConnected }
        .onEach { bleIndication ->
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
        }.catch { logger(TAG) { "listen error : $it" } }

    override fun cancelScanning() = hrDeviceRepo.cancelScanning()

    override fun disconnect() {
        hrDeviceRepo.disconnect()
        jobs.cancelAndClear()
        bleNotification.value = BleNotification.Empty
    }
}
