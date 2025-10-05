package com.achub.hram.data

import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.Flow

interface BleHrDataRepo {
    fun observeHeartRate(peripheral: Peripheral): Flow<Int>
}