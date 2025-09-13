package com.achub.hram.data

import kotlinx.coroutines.flow.Flow

interface BleRepo {
    fun scanHrDevices(): Flow<String>
}