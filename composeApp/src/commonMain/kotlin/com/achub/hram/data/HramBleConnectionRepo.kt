package com.achub.hram.data

import com.achub.hram.BluetoothState
import com.achub.hram.HR_SERVICE_UUID
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi


@Single
class HramBleConnectionRepo(@Provided val bluetoothState: BluetoothState) : BleConnectionRepo {


    override var connected: Peripheral? = null
    override var isBluetoothOn: StateFlow<Boolean> = bluetoothState.isBluetoothOn

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(HR_SERVICE_UUID)
            }
        }
    }.advertisements

    override fun connectToDevice(advertisement: Advertisement): Flow<Peripheral> = flow {
        connected?.disconnect()
        val peripheral = Peripheral(advertisement)
        peripheral.connect()
        connected = peripheral
        emit(peripheral)
    }

    override suspend fun disconnect() {
        connected?.disconnect()
    }

    override fun init() = bluetoothState.init()


    override fun release() = bluetoothState.release()

}
