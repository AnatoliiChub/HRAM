package com.achub.hram.ble.core

import com.achub.hram.ble.model.BleDevice
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface BleConnectionManager {
    var isBluetoothOn: Flow<Boolean>

    val onConnected: Flow<Peripheral>

    fun scanHrDevices(): Flow<Advertisement>

    @OptIn(ExperimentalUuidApi::class)
    fun connectToDevice(identifier: Identifier): Flow<BleDevice>

    suspend fun disconnect()
}
