package com.achub.hram.ble.core.connection

import com.achub.hram.ble.models.BleDevice
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

/**
 * Manages BLE connections, including scanning for devices, connecting to them,
 * and handling connection states.
 */
interface BleConnectionManager {
    val isBluetoothOn: Flow<Boolean>

    val onConnected: Flow<Peripheral>

    fun scanHrDevices(): Flow<Advertisement>

    @OptIn(ExperimentalUuidApi::class)
    fun connectToDevice(identifier: Identifier): Flow<BleDevice>

    suspend fun disconnect()
}
