@file:Suppress("ktlint:standard:filename", "detekt:Filename")

package com.achub.hram.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * JVM (desktop) implementation of [BluetoothState].
 * Emits `true` on start and stays open; Kable throws appropriate exceptions
 * at scan/connect time when the Bluetooth adapter is unavailable.
 */
class BluetoothStateJvm : BluetoothState {
    override val isBluetoothOn: Flow<Boolean> = flow {
        // TODO TO BE IMPLEMENTED:
        emit(true)
        kotlinx.coroutines.awaitCancellation()
    }
}
