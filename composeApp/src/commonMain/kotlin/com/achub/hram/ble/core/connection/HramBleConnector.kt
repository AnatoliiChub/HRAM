package com.achub.hram.ble.core.connection

import com.achub.hram.ext.logger
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private const val TAG = "HramBleConnector"

class HramBleConnector(
    override val connected: MutableStateFlow<Peripheral?> = MutableStateFlow(null),
    private val peripheralBuilder: (Advertisement) -> Peripheral = { Peripheral(it) }
) : BleConnector {
    private var _peripheral: Peripheral? = null

    override suspend fun connect(advertisement: Advertisement): Peripheral {
        val peripheral = peripheralBuilder(advertisement)
        logger(TAG) { "initiate connection to device $peripheral" }
        peripheral.connect()
        _peripheral = peripheral
        connected.update { peripheral }
        logger(TAG) { "connected to device $peripheral" }
        return peripheral
    }

    override suspend fun disconnect() {
        logger(TAG) { "disconnecting from device $_peripheral" }
        _peripheral?.disconnect()
    }
}
