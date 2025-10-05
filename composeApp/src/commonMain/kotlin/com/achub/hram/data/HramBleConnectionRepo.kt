package com.achub.hram.data

import com.achub.hram.HR_SERVICE_UUID
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.uuid.ExperimentalUuidApi


class HramBleConnectionRepo : BleConnectionRepo {
    override var connected: Peripheral? = null

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(HR_SERVICE_UUID)
            }
        }
    }.advertisements

    override fun connectToDevice(advertisement: Advertisement): Flow<Peripheral> = flow{
        connected?.disconnect()
        val peripheral = Peripheral(advertisement)
        peripheral.connect()
        connected = peripheral
        emit(peripheral)
    }

    override suspend fun disconnect() {
        connected?.disconnect()
    }
}
