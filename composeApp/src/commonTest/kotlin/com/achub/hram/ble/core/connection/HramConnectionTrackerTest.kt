package com.achub.hram.ble.core.connection

import com.achub.hram.ble.BluetoothState
import com.achub.hram.identifier
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.State
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HramConnectionTrackerTest {
    lateinit var bluetoothOn: MutableStateFlow<Boolean>
    lateinit var stateFlow: MutableStateFlow<State>
    lateinit var peripheralMock: Peripheral
    lateinit var isKeepConnectionMock: Channel<Boolean>
    lateinit var tracker: HramConnectionTracker

    @BeforeTest
    fun setup() {
        bluetoothOn = MutableStateFlow(true)
        stateFlow = MutableStateFlow(State.Connecting.Bluetooth)
        peripheralMock = createPeripheral(stateFlow)
        isKeepConnectionMock = mock(MockMode.autofill)
        tracker = tracker(bluetoothOn, isKeepConnectionMock)
    }

    @Test
    fun `startTracking - disconnect`() = runTest {
        val trackingJob = tracker.trackConnectionState(peripheralMock).launchIn(this)
        stateFlow.emit(State.Disconnected())
        advanceUntilIdle()

        verify { isKeepConnectionMock.trySend(true) }

        trackingJob.cancel()
    }

    @Test
    fun `startTracking - disconnect, bluetoothOff`() = runTest {
        bluetoothOn.update { false }

        val trackingJob = tracker.trackConnectionState(peripheralMock).launchIn(this)
        stateFlow.emit(State.Disconnected())
        advanceUntilIdle()

        verifyNoMoreCalls(isKeepConnectionMock)

        trackingJob.cancel()
    }

    @Test
    fun `startTracking - connected or connecting states`() = runTest {
        val trackingJob = tracker.trackConnectionState(peripheralMock).launchIn(this)
        stateFlow.emit(State.Connected(this))
        stateFlow.emit(State.Connecting.Services)
        stateFlow.emit(State.Connecting.Bluetooth)
        stateFlow.emit(State.Connecting.Observes)
        advanceUntilIdle()

        verifyNoMoreCalls(isKeepConnectionMock)

        trackingJob.cancel()
    }

    @Test
    fun `observeDisconnection true, false`() = runTest {
        val events = mutableListOf<Boolean>()
        val isKeepConnection = Channel<Boolean>()
        tracker = tracker(bluetoothOn, isKeepConnection)

        val job = tracker.observeDisconnection().onEach { events.add(it) }.launchIn(this)
        isKeepConnection.send(true)
        isKeepConnection.send(false)
        advanceUntilIdle()

        assertTrue(events.first())
        assertFalse(events[1])
        assertEquals(2, events.size)

        job.cancel()
    }

    private fun tracker(bluetoothOn: MutableStateFlow<Boolean>, isKeepConnection: Channel<Boolean>) =
        HramConnectionTracker(
            object : BluetoothState {
                override val isBluetoothOn = bluetoothOn
            },
            isKeepConnection = isKeepConnection
        )

    @OptIn(ExperimentalApi::class)
    private fun createPeripheral(stateFlow: StateFlow<State>): Peripheral =
        mock(MockMode.autofill) {
            every { state } returns stateFlow
            every { name } returns "Peripheral"
            every { identifier } returns identifier("Identifier")
        }
}
