package com.achub.hram

import kotlinx.coroutines.flow.StateFlow

interface BluetoothState {
    val isBluetoothOn : StateFlow<Boolean>

    fun init()

    fun release()
}
