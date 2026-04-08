package com.achub.hram.tracking

import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.BleState
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

// TrackingStateStage moved to its own file `TrackingStateStage.kt`

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
