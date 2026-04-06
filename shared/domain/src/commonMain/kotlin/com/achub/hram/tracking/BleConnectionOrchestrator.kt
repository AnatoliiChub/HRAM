package com.achub.hram.tracking

import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.BleState
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface BleConnectionOrchestrator {
    /** Cold flow of raw (un-enriched) BLE notifications. Handles BLE state transitions internally. */
    fun connectAndSubscribe(device: DeviceModel): Flow<BleNotificationModel>

    fun scan(duration: Duration): Flow<ScanResultModel>

    fun cancelScanning()

    suspend fun disconnect()

    fun observeBleState(): Flow<BleState>

    fun reportNotification(notification: BleNotificationModel, device: DeviceModel)
}
