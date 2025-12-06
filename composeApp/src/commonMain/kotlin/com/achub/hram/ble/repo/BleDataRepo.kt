package com.achub.hram.ble.repo

import com.achub.hram.ble.model.HrNotification
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<HrNotification>

    fun observeBatteryLevel(peripheral: Peripheral): Flow<Int>
}
