package com.achub.hram.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class HramBleRepo : BleRepo {

    override fun scanHrDevices() = (0..30)
        .asFlow()
        .onEach { delay((300L..700L).random()) }
        .map { "Device ${it.mod(5)}" }
}