package com.achub.hram.tracking

import com.achub.hram.ble.repo.HrDeviceRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.HrIndication
import com.achub.hram.launchIn
import com.achub.hram.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
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
@Single
class HramActivityTrackingManager : ActivityTrackingService, KoinComponent {

    val stopWatch: StopWatch by inject()
    val hrDeviceRepo: HrDeviceRepo by inject(parameters = { parametersOf(scope) })
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackingState = AtomicInt(TRACKING_INIT_STATE)
    private var listenJob: Job? = null
    override val hrIndication = Channel<HrIndication>()
    override fun elapsedTime(): Flow<Long> = stopWatch.listen()
    private val isRecording get() = trackingState.load() == ACTIVE_TRACKING_STATE

    override fun startTracking() {
        trackingState.update { ACTIVE_TRACKING_STATE }
        stopWatch.start()
    }

    override fun pauseTracking() {
        trackingState.update { PAUSED_TRACKING_STATE }
        stopWatch.pause()
    }

    override fun finishTracking() {
        trackingState.update { TRACKING_INIT_STATE }
        stopWatch.reset()
    }

    override fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit) =
        hrDeviceRepo.scan(onInit, onUpdate, onComplete)

    override fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit
    ) = hrDeviceRepo.connect(device, onInitConnection, onConnected).also {
        listen().launchIn(scope, Dispatchers.Default).let { listenJob = it }
    }

    private fun listen() = hrDeviceRepo.listen().onStart { emit(HrIndication.Empty) }
            .onEach { hrIndication.send(it) }
            .filter { isRecording && it.isEmpty().not() }
            .onEach {
                //TODO store to DB
            }.catch { logger(TAG) { "listen error : $it" } }


    override fun cancelScanning() = hrDeviceRepo.cancelScanning()

    override fun disconnect() {
        hrDeviceRepo.disconnect()
        listenJob?.cancel()
        listenJob = null
        hrIndication.trySend(HrIndication.Empty)
    }
}