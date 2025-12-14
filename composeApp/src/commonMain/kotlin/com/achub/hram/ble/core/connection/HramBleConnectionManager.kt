package com.achub.hram.ble.core.connection

import com.achub.hram.ble.models.BleConnectionsException
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.toBleDevice
import com.achub.hram.ext.HR_SERVICE_UUID
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.juul.kable.Advertisement
import com.juul.kable.ExperimentalApi
import com.juul.kable.Identifier
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.PlatformAdvertisement
import com.juul.kable.Scanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "HramBleConnectionManager"
private const val RECONNECTION_RETRY_ATTEMPTS = 3L

class HramBleConnectionManager(
    val connectionTracker: ConnectionTracker,
    @InjectedParam val scope: CoroutineScope
) : BleConnectionManager {
    override val isBluetoothOn: Flow<Boolean> = connectionTracker.isBluetoothOn
    private val _connected = MutableStateFlow<Peripheral?>(null)

    @OptIn(ExperimentalApi::class)
    override val onConnected = _connected.filterNotNull()
    private var connectionTrackerJob: Job? = null

    @OptIn(ExperimentalUuidApi::class, FlowPreview::class)
    override fun scanHrDevices() = Scanner { filters { match { services = listOf(HR_SERVICE_UUID) } } }.advertisements

    @OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
    override fun connectToDevice(identifier: Identifier): Flow<BleDevice> =
        connectionTracker.observeDisconnection()
            .onStart { emit(true) } // for initial connect
            .onEach { stopConnectionTracking() }
            .onEach { _connected.value?.disconnect() }
            .catch { loggerE(TAG) { "Error during disconnection: $it" } }
            .map { scan(identifier) }
            .map { connect(it) }
            .retry(RECONNECTION_RETRY_ATTEMPTS, ::isReconnectionRequired)

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    private suspend fun connect(advertisement: Advertisement): BleDevice {
        stopConnectionTracking()
        val peripheral = Peripheral(advertisement)
        logger(TAG) { "initiate connection to device $peripheral" }
        peripheral.connect()
        logger(TAG) { "connected to device $peripheral" }
        _connected.update { peripheral }
        startConnectionTracking(peripheral)
        return peripheral.toBleDevice()
    }

    override suspend fun disconnect() {
        connectionTracker.stopTracking()
        _connected.value?.disconnect()
        _connected.value = null
        stopConnectionTracking()
    }

    private suspend fun scan(identifier: Identifier): PlatformAdvertisement =
        scanHrDevices().filter { it.identifier == identifier }.first()

    private fun startConnectionTracking(peripheral: Peripheral) {
        connectionTrackerJob = connectionTracker.startTracking(peripheral, onCompletion = ::stopConnectionTracking)
            .launchIn(scope)
    }

    private fun stopConnectionTracking() {
        connectionTrackerJob?.cancel()
        connectionTrackerJob = null
    }

    private fun isReconnectionRequired(throwable: Throwable): Boolean {
        val isReconnectionRequired =
            throwable is BleConnectionsException.DeviceNotConnectedException || throwable is NotConnectedException
        logger(TAG) { "try to reconnect: $isReconnectionRequired, because of $throwable" }
        return isReconnectionRequired
    }
}
