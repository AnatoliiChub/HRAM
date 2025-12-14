package com.achub.hram.ble.core.connection

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.models.BleConnectionsException
import com.achub.hram.ext.logger
import com.achub.hram.ext.loggerE
import com.juul.kable.ExperimentalApi
import com.juul.kable.NotConnectedException
import com.juul.kable.Peripheral
import com.juul.kable.State
import com.juul.kable.UnmetRequirementException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Provided
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "ConnectionTracker"

class HramConnectionTracker(
    @Provided val bluetoothState: BluetoothState,
    private val isKeepConnection: Channel<Boolean> = Channel(Channel.CONFLATED)
) : ConnectionTracker {
    companion object Companion {
        private val CONNECT_STATES = listOf(
            State.Connected::class,
            State.Connecting::class,
        )
    }

    override val isBluetoothOn: Flow<Boolean> = bluetoothState.isBluetoothOn

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class)
    override fun startTracking(peripheral: Peripheral, onCompletion: () -> Unit) = peripheral.state
        .onEach { logger(TAG) { "${peripheral.name} device current connection state $it" } }
        .map { currentState -> CONNECT_STATES.any { it == currentState::class }.not() }
        .combine(isBluetoothOn) { notConnected, isBtOn -> notConnected && isBtOn }
        .filter { it }
        .onEach { throw NotConnectedException(peripheral.identifier.toString()) }
        .catch { handleTrackingFlowError(it) }
        .onCompletion { onCompletion() }

    override fun observeDisconnection(): Flow<Boolean> {
        val isKeepConnectionFlow = isKeepConnection.receiveAsFlow()
            .onEach { logger(TAG) { "Reconnection requested" } }
            .onEach { if (it.not()) throw BleConnectionsException.DisconnectRequestedException() }

        val bluetoothOnEventFlow = isBluetoothOn.filter { it }.onEach { logger(TAG) { "Bluetooth is ON" } }

        return bluetoothOnEventFlow.combine(isKeepConnectionFlow) { _, keepConnection -> keepConnection }
    }

    private fun handleTrackingFlowError(throwable: Throwable) {
        loggerE(TAG) { "Reconnection flow exception: $throwable" }
        if (throwable !is UnmetRequirementException) isKeepConnection.trySend(true)
    }

    override fun stopTracking() {
        isKeepConnection.trySend(false)
    }
}
