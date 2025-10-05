package com.achub.hram

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//TODO IMPLEMENT THIS CLASS
class BluetoothStateIos : BluetoothState {
    override val isBluetoothOn: StateFlow<Boolean> = MutableStateFlow(false)

    override fun init() {
    }

    override fun release() {
    }
}
