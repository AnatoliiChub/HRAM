package com.achub.hram.ble.model

import com.achub.hram.ext.MANUFACTURER_NAME_CHAR_UUID
import com.achub.hram.ext.MANUFACTURER_SERVICE_UUID
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import kotlin.uuid.ExperimentalUuidApi

data class BleDevice(val name: String, val identifier: String, val manufacturer: String? = null)

@OptIn(ExperimentalUuidApi::class)
val MANUFACTURER_CHAR = characteristicOf(MANUFACTURER_SERVICE_UUID, MANUFACTURER_NAME_CHAR_UUID)

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
suspend fun Peripheral.toBleDevice() = BleDevice(
    name = name ?: identifier.toString(),
    identifier = identifier.toString(),
    manufacturer = read(MANUFACTURER_CHAR).decodeToString()
)
