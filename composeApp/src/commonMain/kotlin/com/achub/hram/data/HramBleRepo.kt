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

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class, ExperimentalApi::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(Uuid.service("heart_rate")) // SensorTag
            }
        }
    }.advertisements
        .map { BleDevice(name = it.peripheralName ?: "", identifier = it.identifier.toString()) }

}

