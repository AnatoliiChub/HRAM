package com.achub.hram.ble.core.connection

import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleConnectionsException
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.PeripheralConvertor
import com.achub.hram.ext.logger
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.UnmetRequirementException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import org.koin.core.annotation.InjectedParam
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleConnectionManager"

/** Number of attempts to retry reconnection in case of disconnection or error */
const val RECONNECTION_RETRY_ATTEMPTS = 3L

/** Delay between reconnection attempts */
const val RECONNECTION_DELAY_MS = 2_000L
private val ERROR_REQUIRED_RECONNECTION = listOf(
    BleConnectionsException.DeviceNotConnectedException::class,
    NotConnectedException::class,
    UnmetRequirementException::class,
)

class HramBleConnectionManager(
    val connectionTracker: ConnectionTracker,
    val scanner: BleScanner,
    val connector: BleConnector,
    val peripheralConverter: PeripheralConvertor,
    @InjectedParam val scope: CoroutineScope,
) : BleConnectionManager {
    @OptIn(ExperimentalApi::class)
    override val onConnected = connector.connected.filterNotNull()

    override fun scanHrDevices() = scanner.scan()

    private var connectionTrackerJob: Job? = null

    @OptIn(ExperimentalAtomicApi::class)
    private val wasConnected = AtomicBoolean(false)

    @OptIn(
        ExperimentalApi::class,
        ExperimentalCoroutinesApi::class,
        ExperimentalUuidApi::class,
        ExperimentalAtomicApi::class
    )
    override fun connectToDevice(identifier: Identifier): Flow<BleDevice> =
        connectionTracker.observeDisconnection()
            .onStart { wasConnected.store(false) }
            .onStart { emit(true) } // for initial connect
            .onEach { stopConnectionTracking() }
            .onEach { connector.disconnect() }
            .map { scanner.scan(identifier, BLE_SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS)) }
            .map(connector::connect)
            .onEach { wasConnected.store(true) }
            .onEach(::startConnectionTracking)
            .map(peripheralConverter::convert)
            .retry(RECONNECTION_RETRY_ATTEMPTS, ::isReconnectionRequired)

    override suspend fun disconnect() {
        stopConnectionTracking()
        connector.disconnect()
    }

    private fun startConnectionTracking(peripheral: Peripheral) {
        connectionTrackerJob = connectionTracker.trackConnectionState(peripheral).launchIn(scope)
    }

    private fun stopConnectionTracking() {
        connectionTrackerJob?.cancel()
        connectionTrackerJob = null
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun isReconnectionRequired(throwable: Throwable): Boolean {
        val isReconnectionRequired = ERROR_REQUIRED_RECONNECTION.any { throwable::class == it } ||
            (throwable is TimeoutCancellationException && wasConnected.load())
        logger(TAG) { "try to reconnect: $isReconnectionRequired, because of $throwable" }
        if (isReconnectionRequired) delay(RECONNECTION_DELAY_MS)
        return isReconnectionRequired
    }
}
