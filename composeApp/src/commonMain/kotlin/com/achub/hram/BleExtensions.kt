package com.achub.hram

import androidx.compose.runtime.Composable
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.characteristic
import com.juul.kable.characteristicOf
import com.juul.kable.service
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val HR_SERVICE_UUID = Uuid.service("heart_rate")

@OptIn(ExperimentalUuidApi::class, ExperimentalApi::class)
val HR_MEASUREMENT_CHAR_UUID = Uuid.characteristic("heart_rate_measurement")

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val MANUFACTURER_NAME_UUID = Uuid.characteristic("manufacturer_name_string")

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val MANUFACTURER_SERVICE_UUID = Uuid.service("device_information")

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val BATTERY_SERVICE_UUID = Uuid.service("battery_service")

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val BATTERY_LEVEL_CHAR_UUID = Uuid.characteristic("battery_level")
fun ByteArray.uint8(offset: Int) = unsignedByteToInt(this[offset])
fun ByteArray.uint16(offset: Int) = unsignedBytesToInt(this[offset], this[offset + 1])
private fun unsignedByteToInt(b: Byte): Int {
    return b.toInt() and 0xFF
}

private fun unsignedBytesToInt(b0: Byte, b1: Byte): Int {
    return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) shl 8))
}

@OptIn(ExperimentalUuidApi::class)
suspend fun Peripheral.readManufacturerName(): String {
    return read(
        characteristicOf(
            Uuid.parse("0000180a-0000-1000-8000-00805f9b34fb"),
            Uuid.parse("00002a29-0000-1000-8000-00805f9b34fb")
        )
    ).decodeToString()
}

@OptIn(ExperimentalUuidApi::class)
suspend fun Peripheral.readBatteryLevel(): Int {
    return read(characteristicOf(BATTERY_SERVICE_UUID, BATTERY_LEVEL_CHAR_UUID)).uint8(0)
}

@OptIn(ExperimentalUuidApi::class)
suspend fun Peripheral.read(service: String, char: String): ByteArray {
    return read(
        characteristicOf(
            Uuid.parse(service),
            Uuid.parse(char)
        )
    )
}

@Suppress("ComposableNaming")
@Composable
expect fun requestBluetooth()
