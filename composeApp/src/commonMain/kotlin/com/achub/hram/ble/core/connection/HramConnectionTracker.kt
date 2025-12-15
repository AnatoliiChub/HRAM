package com.achub.hram.ble.core.connection

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ext.logger
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.State
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Provided
import kotlin.uuid.ExperimentalUuidApi

private const val TAG = "ConnectionTracker"
private const val STATE_CHANGING_DEBOUNCE = 500L

class HramConnectionTracker(
    @Provided val bluetoothState: BluetoothState,
    private val isKeepConnection: Channel<Boolean> = Channel(CONFLATED)
) : ConnectionTracker {
    companion object {
        private val CONNECT_STATES = listOf(
            State.Connected::class,
            State.Connecting.Bluetooth::class,
            State.Connecting.Services::class,
            State.Connecting.Observes::class,
        )
    }

    override val isBluetoothOn: Flow<Boolean> = bluetoothState.isBluetoothOn

    @OptIn(ExperimentalApi::class, ExperimentalUuidApi::class, FlowPreview::class)
    override fun trackConnectionState(peripheral: Peripheral) = peripheral.state
        .onEach { logger(TAG) { "${peripheral.name} device current connection state $it" } }
        .map { currentState -> CONNECT_STATES.any { it == currentState::class }.not() }
        .debounce(STATE_CHANGING_DEBOUNCE)
        .combine(isBluetoothOn) { isDisconnected, isBluetoothOn -> isDisconnected && isBluetoothOn }
        .filter { it }
        .onEach { isKeepConnection.trySend(true) }

    override fun observeDisconnection(): Flow<Boolean> = isKeepConnection.receiveAsFlow()
        .onEach { logger(TAG) { "Keep connection emitted $it" } }
}
