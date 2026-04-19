package com.achub.hram.ble.core.connection

import com.achub.hram.Logger
import com.achub.hram.ble.BLE_SCAN_DURATION
import com.achub.hram.ble.models.BleConnectionsException
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.PeripheralConvertor
import com.juul.kable.Advertisement
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
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.milliseconds
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

internal class HramBleConnectionManager(
    val connectionTracker: ConnectionTracker,
    val scanner: BleScanner,
    val connector: BleConnector,
    val peripheralConverter: PeripheralConvertor,
    val scope: CoroutineScope,
    val advertisementCache: AdvertisementCache = AdvertisementCache(),
) : BleConnectionManager {
    @OptIn(ExperimentalApi::class)
    override val onConnected = connector.connected.filterNotNull()

    @OptIn(ExperimentalUuidApi::class)
    override fun scanHrDevices() = scanner.scan()
        .onStart { advertisementCache.clear() }
        .onEach { advertisement -> advertisementCache.put(advertisement) }

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
            .onStart { emit(true) } // for initial connect
            .onEach { stopConnectionTracking() }
            .onEach { connector.disconnect() }
            .map { resolveAdvertisement(identifier) }
            .map(connector::connect)
            .onEach(::startConnectionTracking)
            .onEach { wasConnected.store(true) }
            .map(peripheralConverter::convert)
            .retry(RECONNECTION_RETRY_ATTEMPTS, ::isReconnectionRequired)

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun resolveAdvertisement(identifier: Identifier): Advertisement {
        val cached = advertisementCache.get(identifier)
        if (cached != null) {
            Logger.d(TAG) { "Using cached advertisement for $identifier" }
            return cached
        }
        Logger.d(TAG) { "No valid cache for $identifier, starting BLE scan" }
        return scanner.scan(identifier, BLE_SCAN_DURATION.milliseconds)
    }

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
        Logger.d(TAG) {
            "try to reconnect: $isReconnectionRequired," +
                " because of $throwable, wasConnected: ${wasConnected.load()}"
        }
        if (isReconnectionRequired) delay(RECONNECTION_DELAY_MS)
        return isReconnectionRequired
    }
}
