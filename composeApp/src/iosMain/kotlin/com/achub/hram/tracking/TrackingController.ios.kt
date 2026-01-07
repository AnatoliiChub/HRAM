package com.achub.hram.tracking

import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.di.CoroutineModule.Companion.WORKER_DISPATCHER
import com.achub.hram.ext.cancelAndClear
import com.achub.hram.ext.launchIn
import com.achub.hram.ext.logger
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

private const val TAG = "TrackingController"

actual class TrackingController : KoinComponent {
    private val tracker: ActivityTrackingManager by inject()
    private val dispatcher: CoroutineDispatcher by inject(qualifier = named(WORKER_DISPATCHER))

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private val jobs = mutableListOf<Job>()

    private var currentAction: Action? = null

    // Live Activity Manager for iOS
    private val liveActivityManager = LiveActivityManager()

    init {
        logger(TAG) { "TrackingController initialized" }
        // Start observing BLE state for Live Activities
        liveActivityManager.startObserving(tracker.observeBleState())

        // Observe tracking state changes
        scope.launch {
            while (true) {
                val trackingState = tracker.trackingState()
                liveActivityManager.updateTrackingState(trackingState)
                kotlinx.coroutines.delay(1000) // Check every second
            }
        }
    }

    actual fun scan(id: String?) {
        logger(TAG) { "Scan initiated" }
        currentAction = Action.Scan
        performScan()
    }

    actual fun connectDevice(device: BleDevice) {
        logger(TAG) { "Connect device: $device" }
        currentAction = Action.Connect
        performConnect(device)
    }

    actual fun disconnectDevice() {
        logger(TAG) { "Disconnect device" }
        currentAction = Action.Disconnect
        tracker.disconnect()
    }

    actual fun startTracking() {
        logger(TAG) { "Start tracking" }
        currentAction = Action.StartTracking
        scope.launch { tracker.startTracking() }
    }

    actual fun pauseTracking() {
        logger(TAG) { "Pause tracking" }
        currentAction = Action.PauseTracking
        scope.launch { tracker.pauseTracking() }
    }

    actual fun finishTracking(name: String) {
        logger(TAG) { "Finish tracking with name: $name" }
        currentAction = Action.StopTracking
        scope.launch { tracker.finishTracking(name) }
    }

    actual fun cancelScanning() {
        logger(TAG) { "Cancel scanning" }
        currentAction = Action.CancelScanning
        tracker.cancelScanning()
    }

    @OptIn(FlowPreview::class)
    private fun performScan() {
        tracker.scan(BLE_SCAN_DURATION.milliseconds)
            .filter { currentAction == Action.Scan }
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { jobs.add(it) }
    }

    private fun performConnect(device: BleDevice) {
        val hramDevice = device as? HramBleDevice ?: HramBleDevice(name = device.name, identifier = device.identifier)
        tracker.connectAndSubscribe(device = hramDevice)
            .flowOn(dispatcher)
            .launchIn(scope)
            .let { jobs.add(it) }
    }

    actual fun clear() {
        logger(TAG) { "Cleaning up TrackingController" }
        liveActivityManager.cleanup()
        jobs.cancelAndClear()
        job.cancel()
    }

    private enum class Action {
        Scan,
        Connect,
        Disconnect,
        StartTracking,
        PauseTracking,
        StopTracking,
        CancelScanning
    }
}
