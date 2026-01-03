package com.achub.hram.ble

import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

sealed interface ScanResult {
    data class ScanUpdate(val device: BleDevice) : ScanResult

    data class Error(val error: Throwable) : ScanResult

    data object Complete : ScanResult
}

sealed interface ConnectionResult {
    data class Connected(val device: BleDevice) : ConnectionResult

    data class Error(val error: Throwable) : ConnectionResult
}

interface HrDeviceRepo {
    /**
     * Returns a flow of BLE notifications with heart rate data
     */
    fun listen(): Flow<BleNotification>

    /**
     * Returns a flow of connection states when connecting to a device
     */
    fun connect(device: BleDevice): Flow<ConnectionResult>

    /**
     * Returns a flow of scan results when scanning for HR devices
     */
    fun scan(duration: Duration): Flow<ScanResult>

    /**
     * Disconnects from the currently connected device
     */
    suspend fun disconnect()
}
