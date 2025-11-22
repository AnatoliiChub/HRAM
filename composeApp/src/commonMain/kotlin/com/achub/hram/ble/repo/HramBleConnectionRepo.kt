package com.achub.hram.ble.repo

import com.achub.hram.HR_SERVICE_UUID
import com.achub.hram.MANUFACTURER_NAME_CHAR_UUID
import com.achub.hram.MANUFACTURER_SERVICE_UUID
import com.achub.hram.ble.BleConnectionsException
import com.achub.hram.ble.BluetoothState
import com.achub.hram.data.models.BleDevice
import com.achub.hram.data.models.toBleDevice
import com.achub.hram.logger
import com.achub.hram.loggerE
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.State
import com.juul.kable.UnmetRequirementException
import com.juul.kable.characteristicOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retry
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleConnectionRepo"

@Single
class HramBleConnectionRepo(
    @Provided val bluetoothState: BluetoothState,
) : BleConnectionRepo {

    companion object {
        private val CONNECT_STATES = listOf(
            State.Connected::class,
            State.Connecting::class,
        )

        @OptIn(ExperimentalUuidApi::class)
        val MANUFACTURER_CHAR = characteristicOf(MANUFACTURER_SERVICE_UUID, MANUFACTURER_NAME_CHAR_UUID)
    }

    override var isBluetoothOn: Flow<Boolean> = bluetoothState.isBluetoothOn
    override var state = Channel<State>()
    override val onConnected: Flow<Peripheral> = state.receiveAsFlow()
        .distinctUntilChanged()
        .filter { it is State.Connected }
        .map { connected }
        .filterNotNull()

    private var connected: Peripheral? = null
    private val isKeepConnection = Channel<Boolean>()
    private var connectionJob: Job? = null
    private var connectionScope: CoroutineScope? = null

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner {
        filters {
            match {
                services = listOf(HR_SERVICE_UUID)
            }
        }
    }.advertisements

    @OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
    override fun connectToDevice(identifier: Identifier): Flow<BleDevice> {
        val isKeepConnectionFlow = isKeepConnection.receiveAsFlow()
            .onEach { logger(TAG) { "Reconnection requested" } }
            .onStart { emit(true) }
            .onEach { if (it.not()) throw BleConnectionsException.DisconnectRequestedException }

        val bluetoothOnEventFlow = isBluetoothOn.filter { it }.onEach { logger(TAG) { "Bluetooth is ON" } }

        return bluetoothOnEventFlow.combine(isKeepConnectionFlow) { isOn, keepConnection -> keepConnection }
            .flatMapLatest {
                flow {
                    runCatching { connected?.disconnect() }.onFailure { loggerE(TAG) { "Error during disconnecting: $it" } }
                    val peripheral = connectByIdentifier(identifier)
                    emit(peripheral.toBleDevice())
                    runConnectionJob(peripheral)
                }
            }.retry(3) {
                it.printStackTrace()
                loggerE(TAG) { "${it}" }
                val tryToReconnect =
                    it is BleConnectionsException.DeviceNotConnectedException || it is NotConnectedException
                logger(TAG) { "try to reconnect: $tryToReconnect" }
                tryToReconnect
            }
    }

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    private suspend fun connectByIdentifier(identifier: Identifier): Peripheral {
        logger(TAG) { "connecting to device $identifier" }
        val advertisement = scanHrDevices().filter { it.identifier == identifier }.first()
        val peripheral = Peripheral(advertisement)
        peripheral.connect()
        logger(TAG) { "connected to ${peripheral.name}" }
        connected = peripheral
        return peripheral
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun runConnectionJob(peripheral: Peripheral) {
        connectionJob?.cancel()
        connectionJob = null
        connectionScope?.cancel()
        connectionScope = CoroutineScope(Dispatchers.Default + SupervisorJob()).apply {
            connectionJob = peripheral.state.onEach { state.trySend(it) }
                .onEach { logger(TAG) { "current state $it" } }
                .map { currentState -> CONNECT_STATES.any { it == currentState::class }.not() }
                .combine(isBluetoothOn) { notConnected, isBtOn -> notConnected && isBtOn }
                .filter { it }
                .onEach { throw NotConnectedException(peripheral.identifier.toString()) }
                .catch {
                    if (it !is UnmetRequirementException) {
                        loggerE(TAG) { "Reconnection requested because of: $it" }
                        isKeepConnection.trySend(true)
                    } else {
                        loggerE(TAG) { "Ignored: $it" }
                    }
                }
                .onCompletion {
                    logger(TAG) { "connectionJob completed" }
                    connectionJob?.cancel()
                    connectionJob = null
                    connectionScope?.cancel()
                }
                .launchIn(this)
        }

    }

    override suspend fun disconnect() {
        isKeepConnection.trySend(false)
        connected?.disconnect()
        connected = null
        connectionJob?.cancel()
        connectionJob = null
        connectionScope?.cancel()
        connectionScope = null
    }

}
