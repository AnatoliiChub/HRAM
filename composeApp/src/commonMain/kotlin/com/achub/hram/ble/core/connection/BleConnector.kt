package com.achub.hram.ble.core.connection

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleConnector {
    val connected: Flow<Peripheral?>

    suspend fun connect(advertisement: Advertisement): Peripheral

    suspend fun disconnect()
}
