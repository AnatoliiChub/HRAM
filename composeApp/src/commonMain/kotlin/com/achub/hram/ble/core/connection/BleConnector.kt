package com.achub.hram.ble.core.connection

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for establishing and managing a single BLE connection.
 */
interface BleConnector {
    /** Flow emitting the currently connected peripheral */
    val connected: Flow<Peripheral?>

    /** Connects to a peripheral based on the provided advertisement */
    suspend fun connect(advertisement: Advertisement): Peripheral

    /** Disconnects from the currently connected peripheral */
    suspend fun disconnect()
}
