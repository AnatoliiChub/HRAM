package com.achub.hram

import androidx.compose.runtime.Composable
import com.juul.kable.ExperimentalApi
import com.juul.kable.characteristic
import com.juul.kable.service
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val HR_SERVICE_UUID = Uuid.service("heart_rate")

@OptIn(ExperimentalUuidApi::class, ExperimentalApi::class)
val HR_MEASUREMENT_CHAR_UUID = Uuid.characteristic("heart_rate_measurement")

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
val MANUFACTURER_NAME_CHAR_UUID = Uuid.characteristic("manufacturer_name_string")

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

@Suppress("ComposableNaming")
@Composable
expect fun requestBluetooth()
