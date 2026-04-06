package com.achub.hram.tracking

import com.achub.hram.data.models.BleState
import com.achub.hram.model.BleNotificationModel
import com.achub.hram.model.DeviceModel
import com.achub.hram.model.ScanResultModel
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

    fun connectAndSubscribe(device: DeviceModel): Flow<BleNotificationModel>

    fun scan(duration: Duration): Flow<ScanResultModel>

    fun releaseState()

    suspend fun trackingState(): TrackingStateStage

    fun cancelScanning()

    fun observeBleState(): Flow<BleState>
}

