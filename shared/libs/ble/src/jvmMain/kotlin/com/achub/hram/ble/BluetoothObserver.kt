package com.achub.hram.ble

import kotlinx.coroutines.flow.Flow

interface BluetoothObserver {
    fun init()

    fun observeBleState(): Flow<Boolean>
}
