package com.achub.hram.ble

import kotlinx.coroutines.flow.Flow

interface BluetoothState {
    val isBluetoothOn: Flow<Boolean>
}
