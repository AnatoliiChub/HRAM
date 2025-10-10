package com.achub.hram.ble.repo

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleConnectionRepo {

    //TODO should be a state instead of the field. Connected state should contain Peripheral
    var connected: Peripheral?

    var isBluetoothOn: StateFlow<Boolean>

    fun scanHrDevices(): Flow<Advertisement>

    fun connectToDevice(advertisement: Advertisement): Flow<Peripheral>

    suspend fun disconnect()

    fun init()

    fun release()
}
