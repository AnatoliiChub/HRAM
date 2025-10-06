package com.achub.hram.data

import com.achub.hram.BATTERY_LEVEL_CHAR_UUID
import com.achub.hram.BATTERY_SERVICE_UUID
import com.achub.hram.HR_MEASUREMENT_CHAR_UUID
import com.achub.hram.HR_SERVICE_UUID
import com.achub.hram.MANUFACTURER_NAME_CHAR_UUID
import com.achub.hram.MANUFACTURER_SERVICE_UUID
import com.achub.hram.uint16
import com.achub.hram.uint8
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class HramBleDataRepo : BleDataRepo {
    @OptIn(ExperimentalUuidApi::class)
    override fun observeHeartRate(peripheral: Peripheral): Flow<Int> = flow {
        peripheral.observe(characteristicOf(HR_SERVICE_UUID, HR_MEASUREMENT_CHAR_UUID))
            .catch { Napier.e { "Error in observeHeartRate: $it" } }
            .map { notification ->
                val flags = notification.uint8(0)
                val is8bitFormat = flags and 1 == 0
                val heartRate = if (is8bitFormat)
                    notification.uint8(1)
                else
                    notification.uint16(1)
                heartRate
            }
            .onEach { Napier.d("hr: $it") }
            .collect { emit(it) }

        peripheral.state.collect {
            Napier.d { "Peripheral state: $it" }
        }
    }.catch { Napier.e { "Error: $it" } }

    @OptIn(ExperimentalUuidApi::class)
    override fun observeBatteryLevel(peripheral: Peripheral) =
        characteristicOf(BATTERY_SERVICE_UUID, BATTERY_LEVEL_CHAR_UUID).let { characteristic ->
            merge(
                peripheral.observe(characteristic),
                flow { emit(peripheral.read(characteristic)) }
            ).map { it.uint8(0) }
        }


    @OptIn(ExperimentalUuidApi::class)
    override suspend fun readManufacturerName(peripheral: Peripheral) = peripheral.read(
        characteristicOf(MANUFACTURER_SERVICE_UUID, MANUFACTURER_NAME_CHAR_UUID)
    ).decodeToString()
}

@OptIn(ExperimentalUuidApi::class)
suspend fun Peripheral.read(service: Uuid, char: Uuid): ByteArray {
    return read(characteristicOf(service, char))
}
