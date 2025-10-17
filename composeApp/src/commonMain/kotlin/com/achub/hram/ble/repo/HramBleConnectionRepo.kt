package com.achub.hram.ble.repo

import com.achub.hram.HR_SERVICE_UUID
import com.achub.hram.ble.BluetoothState
import com.juul.kable.Advertisement
import com.juul.kable.ExperimentalApi
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

@Single
class HramBleConnectionRepo(
    @Provided val bluetoothState: BluetoothState
) : BleConnectionRepo {


    override var connected: Peripheral? = null
    override var isBluetoothOn: StateFlow<Boolean> = bluetoothState.isBluetoothOn
    override var state = MutableStateFlow<State>(State.Disconnected())

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(HR_SERVICE_UUID)
            }
        }
    }.advertisements

    @OptIn(ExperimentalApi::class)
    override fun connectToDevice(advertisement: Advertisement): Flow<Peripheral> = flow {
        connected?.disconnect()
        val peripheral = Peripheral(advertisement)
        peripheral.connect()
        connected = peripheral
        Napier.d { "connected to ${peripheral.name}" }
        emit(peripheral)
        peripheral.state.onEach {
            Napier.d { "new state : $it" }
        }.catch { Napier.e { "State error: $it" } }.collect { state.value = it }
    }

    override suspend fun disconnect() {
        connected?.disconnect()
        connected = null
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun init() {
        bluetoothState.init()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                isBluetoothOn.onEach {
                    if (it && connected != null) {
                        try {
                            reconnect()
                        } catch (ex: Exception) {
                            Napier.d { "Ex: $ex " }
                        }
                    }
                }.catch { Napier.e { "error: $it" } }.collect {
                    Napier.d { "bluetooth on: $it" }
                }
            } catch (exception: Exception) {
                Napier.d { "global : $exception" }
            }

        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun reconnect() {
        try {
            connected?.disconnect()
            Napier.d { "Try to reconnect..." }
            scanHrDevices().filter { it.identifier == connected?.identifier }
                .first()
                .let {
                    val peripheral = Peripheral(it)
                    connected = peripheral
                    state.value = peripheral.state.value
                }
        } catch (exception: NotConnectedException) {
            Napier.e { "exception : $exception" }
        }
    }


    override fun release() = bluetoothState.release()


}
