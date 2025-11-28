package com.achub.hram.ble.repo

import com.achub.hram.ble.model.BleDevice
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import com.juul.kable.Peripheral
import com.juul.kable.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface BleConnectionRepo {

    var isBluetoothOn: Flow<Boolean>

    val state: Channel<State>

    val onConnected: Flow<Peripheral>

    fun scanHrDevices(): Flow<Advertisement>

    @OptIn(ExperimentalUuidApi::class)
    fun connectToDevice(identifier: Identifier): Flow<BleDevice>

    suspend fun disconnect()
}
