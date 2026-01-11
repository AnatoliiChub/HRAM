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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.sample
import org.koin.ext.getFullName

private const val TAG = "LiveActivityManager"

@OptIn(ExperimentalForeignApi::class)
class HramLiveActivityManager : LiveActivityManager {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private var observerJob: Job? = null
    private var currentDeviceName: String = ""
    private var lastBleState: BleState? = null

    /**
     * Starts observing BLE state and updates Live Activity accordingly
     */
    @OptIn(FlowPreview::class)
    override fun startObserving(bleStateFlow: Flow<BleState>, trackingStateFlow: Flow<TrackingStateStage>) {
        logger(TAG) { "Starting Live Activity observation" }

        observerJob?.cancel()
        bleStateFlow
            .combine(trackingStateFlow.onStart { TrackingStateStage.TRACKING_INIT_STATE }) { bleState, trackingState ->
                Pair(bleState, trackingState)
            }.sample(NOTIFICATION_SAMPLE_MS)
            .distinctUntilChanged()
            .onEach { state -> handleStateUpdate(state.first, state.second) }
            .flowOn(Dispatchers.Default)
            .launchIn(scope)
            .let { observerJob = it }
    }

    override fun stopObserving() {
        logger(TAG) { "Stopping Live Activity observation" }
        observerJob?.cancel()
        observerJob = null
        endActivity()
    }

    override fun cleanup() {
        logger(TAG) { "Cleaning up LiveActivityManager" }
        stopObserving()
        job.cancel()
    }

    override fun startActivity(bleState: BleState, trackingStateStage: TrackingStateStage) {
        try {
            val heartRate = (bleState as? BleState.NotificationUpdate)?.bleNotification?.hrNotification?.hrBpm ?: 0
            val isConnected = bleState is BleState.Connected || bleState is BleState.NotificationUpdate
            val isContactOn =
                (bleState as? BleState.NotificationUpdate)?.bleNotification?.hrNotification?.isContactOn ?: false
            val batteryLevel = (bleState as? BleState.NotificationUpdate)?.bleNotification?.batteryLevel ?: 0
            val deviceName = when (bleState) {
                is BleState.Connecting -> bleState.device.name
                is BleState.Connected -> bleState.bleDevice.name
                is BleState.NotificationUpdate -> bleState.device.name
                else -> ""
            }
            val elapsedTime = (bleState as? BleState.NotificationUpdate)?.bleNotification?.elapsedTime ?: 0
            val activityId = LiveActivityBridgeImpl.startActivityWithHeartRate(
                heartRate = heartRate.toLong(),
                isConnected = isConnected,
                isContactOn = isContactOn,
                bleState = bleState::class.getFullName(),
                isTrackingActive = trackingStateStage.isActive(),
                batteryLevel = batteryLevel.toLong(),
                deviceName = deviceName,
                elapsedTime = elapsedTime
            )
            logger(TAG) { "Live Activity started with ID: $activityId" }
        } catch (e: Exception) {
            loggerE(TAG) { "Failed to start Live Activity: ${e.message}" }
        }
    }

    private fun handleStateUpdate(bleState: BleState, trackingState: TrackingStateStage) {
        lastBleState = bleState
        val bleStateType = bleState::class.getFullName()
        val isTrackingActive = trackingState.isActive() == true

        when (bleState) {
            is BleState.Scanning.Started -> {
                startActivity(bleState, trackingState)
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
                startActivity(bleState, trackingState)
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
                endActivity()
            }
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
        try {
            LiveActivityBridgeImpl.updateActivityWithHeartRate(
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

    private fun endActivity() {
        LiveActivityBridgeImpl.endActivity()
    }
}
