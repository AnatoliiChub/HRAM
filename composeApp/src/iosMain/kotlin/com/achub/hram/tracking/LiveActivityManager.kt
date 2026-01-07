package com.achub.hram.tracking

import com.achub.hram.data.models.BleState
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.hram.bridge.LiveActivityBridgeImpl
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch

private const val TAG = "LiveActivityManager"

private const val HRAM_ACTIVITY = "Heart Rate Activity"

@OptIn(ExperimentalForeignApi::class)
class LiveActivityManager {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var observerJob: Job? = null
    private var currentActivityId: String? = null

    private var currentDeviceName: String = ""

    /**
     * Starts observing BLE state and updates Live Activity accordingly
     */
    @OptIn(FlowPreview::class)
    fun startObserving(bleStateFlow: Flow<BleState>) {
        logger(TAG) { "Starting Live Activity observation" }

        observerJob?.cancel()
        observerJob = scope.launch(Dispatchers.Default) {
            bleStateFlow.sample(1000L).collect { state ->
                handleBleStateUpdate(state)
            }
        }
    }

    /**
     * Stops observing and ends the Live Activity
     */
    fun stopObserving() {
        logger(TAG) { "Stopping Live Activity observation" }
        observerJob?.cancel()
        observerJob = null
        endActivity()
    }

    private fun handleBleStateUpdate(state: BleState) {
        when (state) {
            is BleState.Scanning.Started -> {
                ensureActivityStarted(HRAM_ACTIVITY)
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Scanning...",
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Scanning.Update -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Found: ${state.device.name}",
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Scanning.Completed -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Scan complete",
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Scanning.Error -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Scan error",
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Connecting -> {
                currentDeviceName = state.device.name
                ensureActivityStarted(HRAM_ACTIVITY)
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Connecting...",
                    batteryLevel = 0,
                    deviceName = state.device.name
                )
            }

            is BleState.Connected -> {
                currentDeviceName = state.bleDevice.name
                updateActivityState(
                    heartRate = 0,
                    isConnected = true,
                    isContactOn = true,
                    bleState = "Connected",
                    batteryLevel = 0,
                    deviceName = state.bleDevice.name
                )
            }

            is BleState.NotificationUpdate -> {
                currentDeviceName = state.device.name
                if (state.bleNotification.isBleConnected) {
                    ensureActivityStarted(HRAM_ACTIVITY)
                }

                val notification = state.bleNotification
                updateActivityState(
                    heartRate = notification.hrNotification?.hrBpm ?: 0,
                    isConnected = notification.isBleConnected,
                    isContactOn = notification.hrNotification?.isContactOn ?: false,
                    bleState = "Connected",
                    batteryLevel = notification.batteryLevel,
                    deviceName = state.device.name
                )
            }

            is BleState.Disconnected -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = "Disconnected",
                    batteryLevel = 0,
                    deviceName = ""
                )
                // End activity after a short delay when disconnected
                scope.launch {
                    kotlinx.coroutines.delay(2000)
                    endActivity()
                }
            }
        }
    }

    /**
     * Ensures an activity is started only if one doesn't already exist
     */
    private fun ensureActivityStarted(activityName: String) {
        startActivity(activityName)
    }

    /**
     * Updates tracking state text (e.g., "Tracking", "Paused")
     */
    fun updateTrackingState(state: TrackingStateStage) = when (state) {
        TrackingStateStage.TRACKING_INIT_STATE -> ""
        TrackingStateStage.ACTIVE_TRACKING_STATE -> "Tracking"
        TrackingStateStage.PAUSED_TRACKING_STATE -> "Paused"
    }

    private fun startActivity(activityName: String) {
        try {
            logger(TAG) { "Starting Live Activity: $activityName" }

            // Call the Swift bridge to start Live Activity
            val activityId = LiveActivityBridgeImpl.startActivityWithActivityName(
                activityName = activityName,
                heartRate = 0,
                isConnected = false,
                isContactOn = false,
                trackingState = "",
                batteryLevel = 0,
                deviceName = ""
            )

            currentActivityId = activityId
            logger(TAG) { "Live Activity started with ID: $currentActivityId" }
        } catch (e: Exception) {
            loggerE(TAG) { "Failed to start Live Activity: ${e.message}" }
        }
    }

    private fun updateActivityState(
        heartRate: Int,
        isConnected: Boolean,
        isContactOn: Boolean,
        bleState: String,
        batteryLevel: Int,
        deviceName: String,
    ) {
        currentActivityId?.let { activityId ->
            try {
                logger(TAG) {
                    "Updating Live Activity: HR=$heartRate, connected=$isConnected, " +
                        "contact=$isContactOn, state=$bleState, battery=$batteryLevel%, device=$deviceName"
                }

                LiveActivityBridgeImpl.updateActivityWithActivityId(
                    activityId = activityId,
                    heartRate = heartRate.toLong(),
                    isConnected = isConnected,
                    isContactOn = isContactOn,
                    trackingState = bleState,
                    batteryLevel = batteryLevel.toLong(),
                    deviceName = deviceName
                )
            } catch (e: Exception) {
                loggerE(TAG) { "Failed to update Live Activity: ${e.message}" }
            }
        }
    }

    private fun endActivity() {
        currentActivityId?.let { activityId ->
            try {
                logger(TAG) { "Ending Live Activity: $activityId" }

                // Call the Swift bridge to end Live Activity
                LiveActivityBridgeImpl.endActivityWithActivityId(activityId)

                currentActivityId = null
            } catch (e: Exception) {
                loggerE(TAG) { "Failed to end Live Activity: ${e.message}" }
            }
        }
    }

    fun cleanup() {
        logger(TAG) { "Cleaning up LiveActivityManager" }
        stopObserving()
        job.cancel()
    }
}
