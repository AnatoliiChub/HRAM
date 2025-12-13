package com.achub.hram.ble.core.connection

import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

/**
 * Observes Bluetooth state and peripheral connection state to manage reconnections.
 */
interface ConnectionTracker {
    val isBluetoothOn: Flow<Boolean>

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    fun startTracking(peripheral: Peripheral, onCompletion: () -> Unit): Flow<Boolean>

    fun observeDisconnection(): Flow<Boolean>

    fun stopTracking()
}
