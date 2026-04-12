@file:Suppress("ktlint:standard:filename", "detekt:Filename")

package com.achub.hram.ble

import kotlinx.coroutines.flow.Flow

class BluetoothStateJvm(private val observer: BluetoothObserver) : BluetoothState {
    override val isBluetoothOn: Flow<Boolean> = observer.observeBleState()
}
