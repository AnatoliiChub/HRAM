package com.achub.hram.data

import com.achub.hram.HR_MEASUREMENT_CHAR_UUID
import com.achub.hram.HR_SERVICE_UUID
import com.achub.hram.uint16
import com.achub.hram.uint8
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@Single
class HramBleHrDataRepo: BleHrDataRepo {
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
            .collect { emit(it) }

        peripheral.state.collect {
            Napier.d { "Peripheral state: $it" }
        }
    }.catch { Napier.e { "Error: $it" } }
}
