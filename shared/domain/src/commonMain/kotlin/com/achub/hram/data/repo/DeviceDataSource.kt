package com.achub.hram.data.repo

import com.achub.hram.models.BleNotificationModel
import com.achub.hram.models.ConnectionResultModel
import com.achub.hram.models.DeviceModel
import com.achub.hram.models.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Domain-facing abstraction for BLE device operations.
 * Implemented in the data layer; domain never imports BLE types directly.
 */
interface DeviceDataSource {
    fun scan(duration: Duration): Flow<ScanResultModel>

    fun connect(device: DeviceModel): Flow<ConnectionResultModel>

    fun listen(): Flow<BleNotificationModel>

    suspend fun disconnect()
}
