package com.achub.hram.data.repo

import com.achub.hram.domain.model.BleNotificationModel
import com.achub.hram.domain.model.ConnectionResultModel
import com.achub.hram.domain.model.DeviceModel
import com.achub.hram.domain.model.ScanResultModel
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Domain-facing abstraction for BLE device operations.
 * Implemented in the data layer; composeApp never imports BLE types directly.
 */
interface DeviceDataSource {
    fun scan(duration: Duration): Flow<ScanResultModel>
    fun connect(device: DeviceModel): Flow<ConnectionResultModel>
    fun listen(): Flow<BleNotificationModel>
    suspend fun disconnect()
}

