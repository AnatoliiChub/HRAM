package com.achub.hram.ble.repo

import com.achub.hram.BATTERY_LEVEL_CHAR_UUID
import com.achub.hram.BATTERY_SERVICE_UUID
import com.achub.hram.HR_MEASUREMENT_CHAR_UUID
import com.achub.hram.HR_SERVICE_UUID
import com.achub.hram.ble.model.HrIndication
import com.achub.hram.logger
import com.achub.hram.loggerE
import com.achub.hram.uint16
import com.achub.hram.uint8
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleDataRepo"

@OptIn(ExperimentalUuidApi::class)
private val HR_CHAR = characteristicOf(HR_SERVICE_UUID, HR_MEASUREMENT_CHAR_UUID)

@OptIn(ExperimentalUuidApi::class)
private val BATTERY_CHAR = characteristicOf(BATTERY_SERVICE_UUID, BATTERY_LEVEL_CHAR_UUID)

@OptIn(ExperimentalUuidApi::class)
class HramBleDataRepo : BleDataRepo {

    override fun observeHeartRate(peripheral: Peripheral): Flow<HrIndication> = peripheral.observe(HR_CHAR)
        .map(::parseHrIndication)
        .onEach { logger(TAG) { "hrIndication: $it" } }
        .catch { loggerE(TAG) { "Error in observeHeartRate: $it" } }

    override fun observeBatteryLevel(peripheral: Peripheral) =
        BATTERY_CHAR.let { characteristic ->
            merge(peripheral.observe(characteristic), flow { emit(peripheral.read(characteristic)) })
                .map { it.uint8(0) }
        }

    private fun parseHrIndication(notification: ByteArray): HrIndication {
        val flags = notification.uint8(0)
        val is8bitFormat = flags and 1 == 0
        val isSensorContactSupported = flags and 4 > 0
        val sensorContacted = if (isSensorContactSupported) flags and 2 > 0 else true
        val heartRate = if (is8bitFormat) notification.uint8(1) else notification.uint16(1)
        return HrIndication(
            hrBpm = heartRate,
            isSensorContactSupported = isSensorContactSupported,
            isContactOn = sensorContacted
        )
    }
}
