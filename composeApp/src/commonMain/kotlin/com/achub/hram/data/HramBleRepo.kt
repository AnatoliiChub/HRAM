package com.achub.hram.data

import com.achub.hram.data.model.BleDevice
import com.juul.kable.ExperimentalApi
import com.juul.kable.Scanner
import com.juul.kable.service
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class HramBleRepo : BleRepo {
    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    val HR_SERVICE_UUID = Uuid.service("heart_rate")

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(HR_SERVICE_UUID) // SensorTag
            }
        }
    }.advertisements
        .map { BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }

}

