package com.achub.hram.ble.core

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.model.BleConnectionsException
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.ble.model.toBleDevice
import com.achub.hram.ext.HR_SERVICE_UUID
import com.achub.hram.ext.MANUFACTURER_NAME_CHAR_UUID
import com.achub.hram.ext.MANUFACTURER_SERVICE_UUID
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.UnmetRequirementException
import com.juul.kable.characteristicOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Provided
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleConnectionManager"
private const val RECONNECTION_RETRY_ATTEMPTS = 3L

class HramBleConnectionManager(
    @Provided val bluetoothState: BluetoothState,
    @InjectedParam val scope: CoroutineScope
) : BleConnectionManager {
    companion object {
        private val CONNECT_STATES = listOf(
            State.Connected::class,
            State.Connecting::class,
        )

        @OptIn(ExperimentalUuidApi::class)
        val MANUFACTURER_CHAR = characteristicOf(MANUFACTURER_SERVICE_UUID, MANUFACTURER_NAME_CHAR_UUID)
    }

    override var isBluetoothOn: Flow<Boolean> = bluetoothState.isBluetoothOn
    private val _connected = MutableStateFlow<Peripheral?>(null)

    @OptIn(ExperimentalApi::class)
    override val onConnected = _connected.filterNotNull().onEach { peripheral ->
        logger(TAG) { "onConnected flow emitted for ${peripheral.name}" }
        runConnectionHandler(peripheral)
    }
    private val isKeepConnection = Channel<Boolean>()
    private var connectionHandlerJob: Job? = null

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match { services = listOf(HR_SERVICE_UUID) }
        }
    }.advertisements

    @OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
    override fun connectToDevice(identifier: Identifier): Flow<BleDevice> {
        val isKeepConnectionFlow = isKeepConnection.receiveAsFlow()
            .onEach { logger(TAG) { "Reconnection requested" } }
            .onStart { emit(true) }
            .onEach { if (it.not()) throw BleConnectionsException.DisconnectRequestedException() }

        val bluetoothOnEventFlow = isBluetoothOn.filter { it }.onEach { logger(TAG) { "Bluetooth is ON" } }

        return bluetoothOnEventFlow.combine(isKeepConnectionFlow) { _, keepConnection -> keepConnection }
            .onEach { _connected.value?.disconnect() }
            .catch { loggerE(TAG) { "Error during disconnection: $it" } }
            .map { connectByIdentifier(identifier).toBleDevice() }
            .retry(RECONNECTION_RETRY_ATTEMPTS) {
                val tryToReconnect =
                    it is BleConnectionsException.DeviceNotConnectedException || it is NotConnectedException
                logger(TAG) { "try to reconnect: $tryToReconnect, because of $it" }
                tryToReconnect
            }
    }

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    private suspend fun connectByIdentifier(identifier: Identifier): Peripheral {
        clearConnectionHandlerJob()
        logger(TAG) { "connecting to device $identifier" }
        val advertisement = scanHrDevices().filter { it.identifier == identifier }.first()
        val peripheral = Peripheral(advertisement)
        peripheral.connect()
        logger(TAG) { "connected to ${peripheral.name}" }
        _connected.update { peripheral }
        return peripheral
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun runConnectionHandler(peripheral: Peripheral) {
        clearConnectionHandlerJob()
        connectionHandlerJob = peripheral.state
            .onEach { logger(TAG) { "current state $it" } }
            .map { currentState -> CONNECT_STATES.any { it == currentState::class }.not() }
            .combine(isBluetoothOn) { notConnected, isBtOn -> notConnected && isBtOn }
            .filter { it }
            .onEach { throw NotConnectedException(peripheral.identifier.toString()) }
            .catch {
                loggerE(TAG) { "Reconnection flow exception: $it" }
                if (it !is UnmetRequirementException) isKeepConnection.trySend(true)
            }.onCompletion { clearConnectionHandlerJob() }
            .launchIn(scope)
    }

    override suspend fun disconnect() {
        isKeepConnection.trySend(false)
        _connected.value?.disconnect()
        _connected.value = null
        clearConnectionHandlerJob()
    }

    private fun clearConnectionHandlerJob() {
        connectionHandlerJob?.cancel()
        connectionHandlerJob = null
    }
}
