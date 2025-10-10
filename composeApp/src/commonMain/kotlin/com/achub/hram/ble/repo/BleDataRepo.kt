package com.achub.hram.ble.repo

import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface BleDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<Int>

    fun observeBatteryLevel(peripheral: Peripheral): Flow<Int>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun readManufacturerName(peripheral: Peripheral): String
}