package com.achub.hram.ble.repo

import com.achub.hram.data.models.HrIndication
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<HrIndication>

    fun observeBatteryLevel(peripheral: Peripheral): Flow<Int>
}