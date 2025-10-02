package com.achub.hram.data

import com.achub.hram.data.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleRepo {
    fun scanHrDevices(): Flow<BleDevice>
}