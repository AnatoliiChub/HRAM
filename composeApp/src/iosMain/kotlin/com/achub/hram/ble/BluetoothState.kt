package com.achub.hram.ble

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

//TODO IMPLEMENT THIS CLASS
@Single
class BluetoothStateIos : BluetoothState {
    override val isBluetoothOn: StateFlow<Boolean> = MutableStateFlow(false)

    override fun init() {
    }

    override fun release() {
    }
}
