package com.achub.hram.tracking

import com.achub.hram.ble.repo.HrDeviceRepo
import com.achub.hram.data.model.BleDevice
import com.achub.hram.data.model.Indications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(
    FlowPreview::class,
    ExperimentalUuidApi::class,
    ExperimentalCoroutinesApi::class,
    ExperimentalTime::class,
    ExperimentalAtomicApi::class
)
@Single
class HramTrackingManager(val stopWatch: StopWatch, val hrDeviceRepo: HrDeviceRepo) : TrackingManager {

    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val trackingState = AtomicInt(TRACKING_INIT_STATE)

    private val isRecording get() = trackingState.load() == ACTIVE_TRACKING_STATE

    override fun startTracking() {
        trackingState.update { ACTIVE_TRACKING_STATE }
        scope.launch { stopWatch.start() }
    }

    override fun pauseTracking() {
        trackingState.update { PAUSED_TRACKING_STATE }
        scope.launch { stopWatch.pause() }
    }

    override fun finishTracking() {
        trackingState.update { TRACKING_INIT_STATE }
        scope.launch { stopWatch.reset() }
    }

    override fun scan(onInit: () -> Unit, onUpdate: (List<BleDevice>) -> Unit, onComplete: () -> Unit) =
        hrDeviceRepo.scan(onInit, onUpdate, onComplete)

    override fun connect(
        device: BleDevice,
        onInitConnection: () -> Unit,
        onConnected: (BleDevice) -> Unit
    ) = hrDeviceRepo.connect(device, onInitConnection, onConnected)

    override fun listen() = hrDeviceRepo.latestIndications.receiveAsFlow()
        .combine(stopWatch.listen().onStart { emit(0) }) { hrIndications, elapsedTime ->
            if (isRecording) {
                //TODO store indication data with timestamp
            }
            Indications(hrIndications, 0f, elapsedTime)
        }

    override fun cancelScanning() = hrDeviceRepo.cancelScanning()

    override fun release() {
        hrDeviceRepo.release()
    }

}