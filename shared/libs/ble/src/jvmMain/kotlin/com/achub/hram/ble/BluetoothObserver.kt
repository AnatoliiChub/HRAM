package com.achub.hram.ble

import kotlinx.coroutines.flow.Flow

interface BluetoothObserver {
    fun observeBleState(): Flow<Boolean>
}
