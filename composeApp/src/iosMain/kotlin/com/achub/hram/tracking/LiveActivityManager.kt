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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import org.koin.ext.getFullName

private const val TAG = "LiveActivityManager"

private const val HRAM_ACTIVITY = "Heart Rate Activity"
private const val END_ACTIVITY_DELAY_MS = 2000L
private const val BLE_UPDATES_SAMPLE_MS = 1000L

@OptIn(ExperimentalForeignApi::class)
class LiveActivityManager {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var observerJob: Job? = null
    private var currentActivityId: String? = null

    private var currentDeviceName: String = ""
    private var currentTrackingState: String = ""
    private var lastBleState: BleState? = null

    /**
     * Starts observing BLE state and updates Live Activity accordingly
     */
    @OptIn(FlowPreview::class)
    fun startObserving(bleStateFlow: Flow<BleState>, trackingStateFlow: Flow<TrackingStateStage>) {
        logger(TAG) { "Starting Live Activity observation" }

        observerJob?.cancel()
        bleStateFlow.sample(BLE_UPDATES_SAMPLE_MS)
            .combine(trackingStateFlow.onStart { TrackingStateStage.TRACKING_INIT_STATE }) { bleState, trackingState ->
                Pair(bleState, trackingState)
            }.onEach { state -> handleStateUpdate(state.first, state.second) }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
            .let { observerJob = it }
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

    private fun handleStateUpdate(bleState: BleState, trackingState: TrackingStateStage? = null) {
        lastBleState = bleState
        val bleStateType = bleState::class.getFullName()
        val isTrackingActive = trackingState?.isActive() == true

        when (bleState) {
            is BleState.Scanning.Started -> {
                ensureActivityStarted(HRAM_ACTIVITY)
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Scanning.Update -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = bleState.device.name
                )
            }

            is BleState.Scanning.Completed -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Scanning.Error -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = ""
                )
            }

            is BleState.Connecting -> {
                currentDeviceName = bleState.device.name
                ensureActivityStarted(HRAM_ACTIVITY)
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = bleState.device.name
                )
            }

            is BleState.Connected -> {
                currentDeviceName = bleState.bleDevice.name
                updateActivityState(
                    heartRate = 0,
                    isConnected = true,
                    isContactOn = true,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = bleState.bleDevice.name
                )
            }

            is BleState.NotificationUpdate -> {
                currentDeviceName = bleState.device.name
                if (bleState.bleNotification.isBleConnected) {
                    ensureActivityStarted(HRAM_ACTIVITY)
                }

                val notification = bleState.bleNotification
                updateActivityState(
                    heartRate = notification.hrNotification?.hrBpm ?: 0,
                    isConnected = notification.isBleConnected,
                    isContactOn = notification.hrNotification?.isContactOn ?: false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = notification.batteryLevel,
                    deviceName = bleState.device.name,
                    elapsedTime = notification.elapsedTime
                )
            }

            is BleState.Disconnected -> {
                updateActivityState(
                    heartRate = 0,
                    isConnected = false,
                    isContactOn = false,
                    bleState = bleStateType,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = 0,
                    deviceName = "",
                    elapsedTime = 0
                )
                // End activity after a short delay when disconnected
                scope.launch {
                    kotlinx.coroutines.delay(END_ACTIVITY_DELAY_MS)
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
    fun updateTrackingState(state: TrackingStateStage) {
        currentTrackingState = when (state) {
            TrackingStateStage.TRACKING_INIT_STATE -> ""
            TrackingStateStage.ACTIVE_TRACKING_STATE -> "Tracking"
            TrackingStateStage.PAUSED_TRACKING_STATE -> "Paused"
        }

        // Re-apply the last BLE state update with the new tracking state
        lastBleState?.let { handleStateUpdate(it) }
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
                bleState = BleState.Disconnected::class.getFullName(),
                isTrackingActive = false,
                batteryLevel = 0,
                deviceName = "",
                elapsedTime = 0
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
        isTrackingActive: Boolean,
        batteryLevel: Int,
        deviceName: String,
        elapsedTime: Long = 0
    ) {
        currentActivityId?.let { activityId ->
            try {
                logger(TAG) {
                    "Updating Live Activity: HR=$heartRate, connected=$isConnected, " +
                        "contact=$isContactOn, state=$bleState, battery=$batteryLevel%, device=$deviceName, time=$elapsedTime"
                }

                LiveActivityBridgeImpl.updateActivityWithActivityId(
                    activityId = activityId,
                    heartRate = heartRate.toLong(),
                    isConnected = isConnected,
                    isContactOn = isContactOn,
                    bleState = bleState,
                    isTrackingActive = isTrackingActive,
                    batteryLevel = batteryLevel.toLong(),
                    deviceName = deviceName,
                    elapsedTime = elapsedTime
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
