package com.achub.hram.data

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleConnectionRepo {

    //TODO should be a state instead of the field. Connected state should contain Peripheral
    var connected: Peripheral?

    fun scanHrDevices(): Flow<Advertisement>

    fun connectToDevice(advertisement: Advertisement): Flow<Peripheral>

    suspend fun disconnect()
}