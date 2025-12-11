package com.achub.hram.ble.model

import com.achub.hram.ble.core.HramBleConnectionManager.Companion.MANUFACTURER_CHAR
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import kotlin.uuid.ExperimentalUuidApi

data class BleDevice(val name: String, val identifier: String, val manufacturer: String? = null)

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
suspend fun Peripheral.toBleDevice() = BleDevice(
    name = name ?: identifier.toString(),
    identifier = identifier.toString(),
    manufacturer = read(MANUFACTURER_CHAR).decodeToString()
)
