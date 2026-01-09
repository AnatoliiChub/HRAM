package com.achub.hram.tracking

import com.achub.hram.ble.ScanResult
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.data.models.BleState
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
enum class TrackingStateStage {
    TRACKING_INIT_STATE,
    ACTIVE_TRACKING_STATE,
    PAUSED_TRACKING_STATE;

    fun isActive() = this == ACTIVE_TRACKING_STATE
}

interface ActivityTrackingManager {
    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String?)

    fun disconnect()

    fun connectAndSubscribe(device: BleDevice): Flow<BleNotification>

    fun scan(duration: Duration): Flow<ScanResult>

    fun releaseState()

    suspend fun trackingState(): TrackingStateStage

    fun cancelScanning()

    fun observeBleState(): Flow<BleState>
}
