package com.achub.hram.data

import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

interface BleHrDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<Int>

    fun observeBatteryLevel(peripheral: Peripheral): Flow<Int>

    @OptIn(ExperimentalUuidApi::class)
    suspend fun readManufacturerName(peripheral: Peripheral): String

    @OptIn(ExperimentalUuidApi::class)
    suspend fun readBatteryLevel(peripheral: Peripheral): Int
}