package com.achub.hram.ble.repo

import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<Int>

    fun observeBatteryLevel(peripheral: Peripheral): Flow<Int>
}