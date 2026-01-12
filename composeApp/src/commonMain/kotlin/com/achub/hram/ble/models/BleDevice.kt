package com.achub.hram.ble.models

import com.achub.hram.OpenForMokkery
import com.achub.hram.ext.MANUFACTURER_NAME_CHAR_UUID
import com.achub.hram.ext.MANUFACTURER_SERVICE_UUID
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import com.juul.kable.toIdentifier
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi

@OpenForMokkery
@Serializable
sealed interface BleDevice {
    val name: String
    val identifier: String
    val manufacturer: String?

    @OptIn(ExperimentalUuidApi::class)
    fun provideIdentifier(): Identifier
}

@OpenForMokkery
@Serializable
data class HramBleDevice(
    override val name: String,
    override val identifier: String,
    override val manufacturer: String? = null
) : BleDevice {
    @OptIn(ExperimentalUuidApi::class)
    override fun provideIdentifier() = identifier.toIdentifier()
}

@OptIn(ExperimentalUuidApi::class)
val MANUFACTURER_CHAR = characteristicOf(MANUFACTURER_SERVICE_UUID, MANUFACTURER_NAME_CHAR_UUID)

@OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
suspend fun Peripheral.toBleDevice(): BleDevice = HramBleDevice(
    name = name ?: identifier.toString(),
    identifier = identifier.toString(),
    manufacturer = read(MANUFACTURER_CHAR).decodeToString()
)

interface PeripheralConvertor {
    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    suspend fun convert(peripheral: Peripheral): BleDevice
}

class HramPeripheralConvertor : PeripheralConvertor {
    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    override suspend fun convert(peripheral: Peripheral): BleDevice = peripheral.toBleDevice()
}
