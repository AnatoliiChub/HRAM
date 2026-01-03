package com.achub.hram.tracking

import com.achub.hram.ble.ConnectionResult
import com.achub.hram.ble.ScanResult
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
enum class TrackingStateStage {
    TRACKING_INIT_STATE,
    ACTIVE_TRACKING_STATE,
    PAUSED_TRACKING_STATE
}

interface ActivityTrackingManager {
    fun startTracking()

    fun pauseTracking()

    fun finishTracking(name: String?)

    fun disconnect()

    fun connect(device: BleDevice): Flow<ConnectionResult>

    fun scan(duration: Duration): Flow<ScanResult>

    fun listen(): Flow<BleNotification>
}
