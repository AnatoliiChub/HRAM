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
    override suspend fun connect(advertisement: Advertisement): Peripheral {
        logger(TAG) { "initiate connection to device $advertisement" }
        val peripheral = peripheralBuilder(advertisement)
        peripheral.connect()
        connected.update { peripheral }
        logger(TAG) { "connected to device $peripheral" }
        return peripheral
    }

    override suspend fun disconnect() {
        logger(TAG) { "disconnecting from device ${connected.value}" }
        connected.value?.disconnect()
        connected.value = null
    }
}
