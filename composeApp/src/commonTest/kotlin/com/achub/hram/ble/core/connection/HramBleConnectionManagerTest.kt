package com.achub.hram.ble.core.connection

import com.achub.hram.ble.models.BleConnectionsException
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.PeripheralConvertor
import com.achub.hram.identifier
import com.juul.kable.Advertisement
import com.juul.kable.ExperimentalApi
import com.juul.kable.Peripheral
import com.juul.kable.State
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.resetCalls
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HramBleConnectionManagerTest {
    companion object {
        const val IDENTIFIER_VALUE = "ble-device-id"
    }

    // Dependencies
    private lateinit var scannerMock: BleScanner
    private lateinit var connectorMock: BleConnector
    private lateinit var trackerMock: ConnectionTracker
    private lateinit var peripheralConvertorMock: PeripheralConvertor

    // Data mocks
    private lateinit var advertisementMock: Advertisement
    private lateinit var peripheralMock: Peripheral
    private lateinit var bleDeviceMock: BleDevice
    private lateinit var trackConnectionMock: Flow<Boolean>
    private lateinit var disconnectionFlow: MutableStateFlow<Boolean>
    private lateinit var collectedDevices: MutableList<BleDevice>
    private val identifier = identifier(IDENTIFIER_VALUE)

    @BeforeTest
    fun setup() {
        scannerMock = mock(MockMode.autofill)
        connectorMock = mock(MockMode.autofill)
        trackerMock = mock(MockMode.autofill)
        advertisementMock = mock(MockMode.autofill)
        bleDeviceMock = mock(MockMode.autofill)
        peripheralConvertorMock = mock(MockMode.autofill)
        peripheralMock = createPeripheral(MutableStateFlow(State.Disconnected()))
        trackConnectionMock = mock(MockMode.autofill)
        disconnectionFlow = MutableStateFlow(false)
        collectedDevices = mutableListOf()
        every { trackerMock.trackConnectionState(peripheralMock) } returns trackConnectionMock
        everySuspend { peripheralConvertorMock.convert(peripheralMock) } returns bleDeviceMock
        every { bleDeviceMock.provideIdentifier() } returns identifier
        everySuspend { connectorMock.connect(advertisementMock) } returns peripheralMock
        every { trackerMock.observeDisconnection() } returns disconnectionFlow.filter { it }
    }

    @Test
    fun scanHrDevices() = runTest {
        every { scannerMock.scan() } returns flowOf(advertisementMock)

        val result = createManager().scanHrDevices()

        var collected: Advertisement? = null
        result.collect { collected = it }
        assertEquals(advertisementMock, collected)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun `connectToDevice - initial`() = runTest {
        everySuspend { scannerMock.scan(identifier, any()) } returns advertisementMock

        val job = createManager().connectToDevice(identifier).onEach { collectedDevices.add(it) }.launchIn(this)
        advanceUntilIdle()

        assertEquals(1, collectedDevices.size)
        assertEquals(identifier, collectedDevices.first().provideIdentifier())
        verifySuspend(VerifyMode.order) {
            scannerMock.scan(identifier, any())
            connectorMock.connect(advertisementMock)
            trackConnectionMock.launchIn(this@runTest)
        }
        job.cancel()
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun `connectToDevice - no devices found`() = runTest {
        val id = identifier("No matches id")
        val exception = IllegalStateException("No devices found")
        everySuspend { scannerMock.scan(id, any()) } calls { throw exception }
        val manager = createManager()

        assertFailsWith<IllegalStateException> { manager.connectToDevice(id).collect { collectedDevices.add(it) } }

        assertEquals(0, collectedDevices.size)
        verifySuspend { connectorMock.disconnect() }
        verifySuspend { trackConnectionMock.launchIn(this@runTest) }
        verifySuspend(VerifyMode.order) { scannerMock.scan(id, any()) }
        verifyNoMoreCalls(trackConnectionMock, connectorMock)
    }

    @Test
    fun `disconnect - delegates to tracker`() = runTest {
        createManager().disconnect()
        advanceUntilIdle()

        verifySuspend { connectorMock.disconnect() }
    }

    @Test
    fun `connect to device successful`() = runTest {
        everySuspend { scannerMock.scan(identifier, any()) } returns advertisementMock

        val job = createManager().connectToDevice(identifier).onEach { collectedDevices.add(it) }.launchIn(this)

        advanceUntilIdle()
        resetCalls(scannerMock, connectorMock, trackConnectionMock)
        disconnectionFlow.update { true }
        advanceUntilIdle()

        assertEquals(identifier, collectedDevices.last().provideIdentifier())
        verifySuspend(VerifyMode.exhaustiveOrder) {
            connectorMock.disconnect()
            scannerMock.scan(identifier, any())
            connectorMock.connect(advertisementMock)
            trackConnectionMock.launchIn(this@runTest)
        }
        job.cancel()
    }

    @Test
    fun `Connect to device failed, reconnection failed`() = runTest {
        val scanException = BleConnectionsException.DeviceNotConnectedException("")
        everySuspend { scannerMock.scan(identifier, any()) } calls { throw scanException }
        var caughtException: Throwable? = null

        val job = createManager().connectToDevice(identifier).onEach { collectedDevices.add(it) }
            .catch { caughtException = it }
            .launchIn(this)

        resetCalls(scannerMock, connectorMock, trackConnectionMock)
        collectedDevices.clear()
        advanceUntilIdle()

        assertTrue(collectedDevices.isEmpty())
        assertEquals(scanException, caughtException)
        val mode = VerifyMode.exactly(RECONNECTION_RETRY_ATTEMPTS.toInt() + 1)
        verifySuspend(mode) { connectorMock.disconnect() }
        verifySuspend(mode) { scannerMock.scan(identifier, any()) }
        verifyNoMoreCalls(connectorMock, trackConnectionMock)
        job.cancel()
    }

    @Test
    fun `Connect to device failed, 1st reconnection successful`() = runTest {
        val scanException = BleConnectionsException.DeviceNotConnectedException("")
        everySuspend { scannerMock.scan(identifier, any()) } calls { throw scanException }
        var caughtException: Throwable? = null

        val job = createManager().connectToDevice(identifier).onEach { collectedDevices.add(it) }
            .catch { caughtException = it }
            .launchIn(this)

        advanceTimeBy(1L)
        resetCalls(scannerMock, connectorMock, trackConnectionMock)
        everySuspend { scannerMock.scan(identifier, any()) } returns advertisementMock
        advanceTimeBy(RECONNECTION_DELAY_MS)

        assertEquals(collectedDevices.size, 1)
        assertEquals(null, caughtException)
        assertEquals(identifier, collectedDevices.last().provideIdentifier())
        verifySuspend(VerifyMode.exhaustiveOrder) {
            connectorMock.disconnect()
            scannerMock.scan(identifier, any())
            connectorMock.connect(advertisementMock)
            trackConnectionMock.launchIn(this@runTest)
        }
        verifyNoMoreCalls(connectorMock, scannerMock)
        job.cancel()
    }

    private fun TestScope.createManager(): HramBleConnectionManager {
        return HramBleConnectionManager(
            connectionTracker = trackerMock,
            scanner = scannerMock,
            connector = connectorMock,
            peripheralConverter = peripheralConvertorMock,
            scope = this,
        ).apply { resetCalls(connectorMock) }
    }

    @OptIn(ExperimentalApi::class)
    private fun createPeripheral(stateFlow: StateFlow<State>): Peripheral = mock(MockMode.autofill) {
        every { state } returns stateFlow
        every { name } returns "Peripheral"
        every { identifier } returns identifier(IDENTIFIER_VALUE)
    }
}
