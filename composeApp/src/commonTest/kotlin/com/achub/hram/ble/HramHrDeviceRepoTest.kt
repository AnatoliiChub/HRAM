package com.achub.hram.ble

import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.models.BleDevice
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.identifier
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.State
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime

class HramHrDeviceRepoTest {
    companion object {
        private const val TEST_TIME_EPSILON_MS = 10L
        private const val ADV_INTERVAL = 950L
        private val IDENTIFIER = identifier("identifier")
    }

    private lateinit var bleConnectionManagerMock: BleConnectionManager
    private lateinit var bleDataRepoMock: BleDataRepo
    private lateinit var onScanInitMock: Runnable
    private lateinit var onScanCompleteMock: Runnable
    private lateinit var onScanUpdateMock: Runnable
    private lateinit var onScanErrorMock: (Throwable) -> Unit
    private lateinit var onConnectionInitMock: Runnable
    private lateinit var onConnectionCompleteMock: (BleDevice) -> Unit
    private lateinit var onConnectionErrorMock: (Throwable) -> Unit
    private lateinit var advertisementMock: Advertisement
    private lateinit var bleDeviceMock: BleDevice

    @BeforeTest
    fun setup() {
        bleConnectionManagerMock = mock()
        bleDataRepoMock = mock()
        onScanInitMock = mock(MockMode.autofill)
        onScanCompleteMock = mock(MockMode.autofill)
        onScanUpdateMock = mock(MockMode.autofill)
        onScanErrorMock = mock(MockMode.autofill)
        onConnectionInitMock = mock(MockMode.autofill)
        onConnectionCompleteMock = mock(MockMode.autofill)
        onConnectionErrorMock = mock(MockMode.autofill)
        advertisementMock = mock(MockMode.autofill)
        bleDeviceMock = mock(MockMode.autofill)

        every { bleConnectionManagerMock.scanHrDevices() } returns flow { emit(advertisementMock) }
        every { bleDeviceMock.provideIdentifier() } returns IDENTIFIER
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect to device`() = runTest {
        every { bleConnectionManagerMock.connectToDevice(IDENTIFIER) } returns flow {
            emit(bleDeviceMock)
            emit(bleDeviceMock)
            emit(bleDeviceMock)
        }
        val repo = createRepo()

        repo.connect(
            device = bleDeviceMock,
            onInitConnection = onConnectionInitMock::run,
            onConnected = onConnectionCompleteMock::invoke,
            onError = onConnectionErrorMock::invoke
        )
        testScheduler.advanceUntilIdle()

        verify { onConnectionInitMock.run() }
        verify(VerifyMode.exactly(1)) { onConnectionCompleteMock.invoke(bleDeviceMock) }
        verifyNoMoreCalls(onConnectionInitMock, onConnectionCompleteMock, onConnectionErrorMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect to device - error`() = runTest {
        val exception = RuntimeException("Connection failed")
        every { bleConnectionManagerMock.connectToDevice(IDENTIFIER) } returns flow { throw exception }

        val repo = createRepo()

        repo.connect(
            device = bleDeviceMock,
            onInitConnection = onConnectionInitMock::run,
            onConnected = onConnectionCompleteMock::invoke,
            onError = onConnectionErrorMock::invoke
        )

        testScheduler.advanceTimeBy(1L)

        verify { onConnectionInitMock.run() }
        verify(VerifyMode.exactly(1)) { onConnectionErrorMock.invoke(exception) }
        verifyNoMoreCalls(onConnectionInitMock, onConnectionCompleteMock, onConnectionErrorMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `listen emits notification, error suppressed`() = runTest {
        val exception = NumberFormatException("Simulated battery read error")
        val peripheral = mock<Peripheral>(MockMode.autofill)
        val hrNotification = HrNotification(hrBpm = 72, isSensorContactSupported = true, isContactOn = true)
        every { bleConnectionManagerMock.onConnected } returns flowOf(peripheral)
        every { bleDataRepoMock.observeHeartRate(peripheral) } returns flowOf(hrNotification)
        every { bleDataRepoMock.observeBatteryLevel(peripheral) } returns flow {
            emit(85)
            delay(100)
            throw exception
        }
        every { peripheral.state } returns MutableStateFlow<State>(State.Connected(this))
        val repo = createRepo()
        val collected = mutableListOf<BleNotification>()

        repo.listen().onEach(collected::add).launchIn(this)

        advanceTimeBy(111L)

        assertEquals(
            BleNotification(hrNotification = hrNotification, batteryLevel = 85, isBleConnected = true),
            collected.first()
        )
        assertEquals(1, collected.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `scan hr devices flow`() = runTest {
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { infiniteAdvertisement() }
        val repo = createRepo()

        repo.scan(
            onInit = onScanInitMock::run,
            onUpdate = { onScanUpdateMock.run() },
            onComplete = onScanCompleteMock::run,
            onError = onScanErrorMock::invoke
        )

        advanceTimeBy(TEST_TIME_EPSILON_MS)

        verify { onScanInitMock.run() }
        verify(VerifyMode.not) { onScanUpdateMock.run() }
        verify(VerifyMode.not) { onScanCompleteMock.run() }

        advanceTimeBy(BLE_SCAN_DURATION)

        verify(VerifyMode.exactly(5)) { onScanUpdateMock.run() }
        verify { onScanCompleteMock.run() }

        advanceTimeBy(BLE_SCAN_DURATION)
        verifyNoMoreCalls(onScanInitMock, onScanUpdateMock, onScanCompleteMock, onScanErrorMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `error during scanning`() = runTest {
        val error = Exception("Scan error")
        every { bleConnectionManagerMock.scanHrDevices() } returns flow {
            delay(ADV_INTERVAL)
            emit(mockAdvertisement(210))
            delay(ADV_INTERVAL)
            throw error
        }
        val repo = createRepo()

        repo.scan(
            onInit = onScanInitMock::run,
            onUpdate = { onScanUpdateMock.run() },
            onComplete = onScanCompleteMock::run,
            onError = onScanErrorMock::invoke
        )
        advanceUntilIdle()

        verify { onScanInitMock.run() }
        verify { onScanUpdateMock.run() }
        verify { onScanCompleteMock.run() }
        verify { onScanErrorMock.invoke(error) }
        verifyNoMoreCalls(onScanInitMock, onScanUpdateMock, onScanCompleteMock, onScanErrorMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun disconnect() = runTest {
        val repo = createRepo()
        everySuspend { bleConnectionManagerMock.disconnect() } returns Unit

        repo.disconnect()

        advanceUntilIdle()
        verifySuspend(VerifyMode.exactly(1)) { bleConnectionManagerMock.disconnect() }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun FlowCollector<Advertisement>.infiniteAdvertisement() {
        var counter = 0L
        while (true) {
            val adv = mockAdvertisement(counter)
            emit(adv)
            counter++
        }
    }

    private suspend fun mockAdvertisement(counter: Long): Advertisement {
        val adv = mock<Advertisement>(MockMode.autofill)
        every { adv.peripheralName } returns "HRM Device$counter"
        every { adv.identifier } returns identifier("identifier-$counter")
        delay(ADV_INTERVAL)
        return adv
    }

    private fun TestScope.createRepo() = HramHrDeviceRepo(
        this,
        bleDataRepoMock,
        StandardTestDispatcher(testScheduler),
        bleConnectionManagerMock
    )
}
