package com.achub.hram.tracking

import com.achub.hram.ble.repo.HrDeviceRepo
import com.achub.hram.cancelAndClear
import com.achub.hram.createActivity
import com.achub.hram.data.HrActivityRepo
import com.achub.hram.data.db.entity.ACTIVE_ACTIVITY
import com.achub.hram.data.db.entity.HeartRateEntity
import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.BleIndication
import com.achub.hram.launchIn
import com.achub.hram.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
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
class HramActivityTrackingManager : ActivityTrackingManager, KoinComponent {

    val stopWatch: StopWatch by inject()
    val hrDeviceRepo: HrDeviceRepo by inject(parameters = { parametersOf(scope) })
    val hrActivityRepo: HrActivityRepo by inject()
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackingState = AtomicInt(TRACKING_INIT_STATE)
    private var jobs = mutableListOf<Job>()
    override val bleIndication = Channel<BleIndication>()
    private val isRecording get() = trackingState.load() == ACTIVE_TRACKING_STATE
    private var currentActId: String? = null

    override fun startTracking() {
        scope.launch(Dispatchers.Default) {
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
        scope.launch(Dispatchers.Default) {
            trackingState.update { TRACKING_INIT_STATE }
            val duration = stopWatch.elapsedTimeSeconds()
            stopWatch.reset()
            val newName = name ?: "${now().epochSeconds}__$duration"
            currentActId?.let { hrActivityRepo.updateNameById(id = it, name = newName, duration = duration) }
            currentActId = null
        }.let { jobs.add(it) }
    }

    override fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit) =
        hrDeviceRepo.scan(onInit, onUpdate, onComplete)

    override fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit
    ) = hrDeviceRepo.connect(device, onInitConnection, onConnected).also {
        listen().flowOn(Dispatchers.Default).launchIn(scope).let { jobs.add(it) }
    }

    private fun listen() = hrDeviceRepo.listen().onStart { emit(BleIndication.Empty) }
        .map { it.copy(elapsedTime = stopWatch.elapsedTimeSeconds()) }
        .onEach { bleIndication.send(it) }
        .filter { isRecording && it.isBleConnected }
        .onEach { bleIndication ->
            bleIndication.hrIndication?.let { hrIndication ->
                currentActId?.let {
                    val entity = HeartRateEntity(
                        activityId = it,
                        heartRate = hrIndication.hrBpm,
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
        bleIndication.trySend(BleIndication.Empty)
    }
}
