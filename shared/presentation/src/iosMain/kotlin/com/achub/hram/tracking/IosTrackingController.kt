package com.achub.hram.tracking

import com.achub.hram.Logger
import com.achub.hram.data.state.BleStateRepo
import com.achub.hram.data.state.TrackingStateRepo
import com.achub.hram.di.CoroutineModule.Companion.WORKER_DISPATCHER
import com.achub.hram.ext.launchIn
import com.achub.hram.models.BleState
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.SCAN_DURATION_MS
import com.achub.hram.tracking.notification.LiveActivityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "IosTrackingController"

class IosTrackingController(private val liveActivityManager: LiveActivityManager) : TrackingController, KoinComponent {
    private val tracker: ActivityTrackingManager by inject()
    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))
    private val trackingStateRepo: TrackingStateRepo by inject()
    private val bleStateRepo: BleStateRepo by inject()

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var scanJob: Job? = null
    private var connectJob: Job? = null
    private var currentAction: ControllerAction? = null

    init {
        Logger.d(TAG) { "IosTrackingController initialized" }
        liveActivityManager.startObserving(bleStateRepo.listen(), trackingStateRepo.listen())
    }

    override fun scan(id: String?) {
        Logger.d(TAG) { "Scan initiated" }
        currentAction = ControllerAction.Scan
        performScan()
    }

    override fun connectDevice(device: DeviceModel) {
        Logger.d(TAG) { "Connect device: $device" }
        currentAction = ControllerAction.Connect
        performConnect(device)
    }

    override fun disconnectDevice() {
        Logger.d(TAG) { "Disconnect device" }
        currentAction = ControllerAction.Disconnect
        tracker.disconnect()
    }

    override fun startTracking() {
        Logger.d(TAG) { "Start tracking" }
        currentAction = ControllerAction.StartTracking
        scope.launch { tracker.startTracking() }
    }

    override fun pauseTracking() {
        Logger.d(TAG) { "Pause tracking" }
        currentAction = ControllerAction.PauseTracking
        scope.launch { tracker.pauseTracking() }
    }

    override fun finishTracking(name: String) {
        Logger.d(TAG) { "Finish tracking with name: $name" }
        currentAction = ControllerAction.StopTracking
        scope.launch { tracker.finishTracking(name) }
    }

    override fun cancelScanning() {
        Logger.d(TAG) { "Cancel scanning" }
        currentAction = ControllerAction.CancelScanning
        tracker.cancelScanning()
    }

    @OptIn(FlowPreview::class)
    private fun performScan() {
        scanJob?.cancel()
        tracker.scan(SCAN_DURATION_MS.milliseconds)
            .filter { currentAction == ControllerAction.Scan }
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { scanJob = it }
    }

    private fun performConnect(device: DeviceModel) {
        connectJob?.cancel()
        tracker.connectAndSubscribe(device = device)
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { connectJob = it }
    }

    override fun clear() {
        Logger.d(TAG) { "Cleaning up IosTrackingController" }
        liveActivityManager.cleanup()
        job.cancel()
        connectJob = null
        scanJob = null
    }

    override fun onAppForeground() {
        scope.launch {
            val bleState = bleStateRepo.get()
            if (bleState != BleState.Disconnected) {
                val trackingState = trackingStateRepo.get()
                liveActivityManager.startActivity(bleState, trackingState)
            }
        }
    }

    private enum class ControllerAction {
        Scan,
        Connect,
        Disconnect,
        StartTracking,
        PauseTracking,
        StopTracking,
        CancelScanning
    }
}
