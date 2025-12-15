package com.achub.hram.ble.core

import com.achub.hram.ble.models.HrNotification
import com.achub.hram.ext.BATTERY_LEVEL_CHAR_UUID
import com.achub.hram.ext.BATTERY_SERVICE_UUID
import com.achub.hram.ext.HR_MEASUREMENT_CHAR_UUID
import com.achub.hram.ext.HR_SERVICE_UUID
import com.juul.kable.Peripheral
import com.juul.kable.characteristicOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleDataRepo"

@OptIn(ExperimentalUuidApi::class)
val HR_CHAR = characteristicOf(HR_SERVICE_UUID, HR_MEASUREMENT_CHAR_UUID)

@OptIn(ExperimentalUuidApi::class)
val BATTERY_CHAR = characteristicOf(BATTERY_SERVICE_UUID, BATTERY_LEVEL_CHAR_UUID)

@OptIn(ExperimentalUuidApi::class)
class HramBleDataRepo(val parser: BleParser) : BleDataRepo {
    override fun observeHeartRate(peripheral: Peripheral): Flow<HrNotification> = peripheral.observe(HR_CHAR)
        .map(parser::parseHrNotification)

    override fun observeBatteryLevel(peripheral: Peripheral) = BATTERY_CHAR.let { characteristic ->
        merge(peripheral.observe(characteristic), flow { emit(peripheral.read(characteristic)) })
            .map(parser::parseBatteryLevel)
    }
}
