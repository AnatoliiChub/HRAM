package com.achub.hram.ble

import com.achub.hram.BLE_SCAN_DURATION
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.models.BleNotification
import com.achub.hram.ble.models.HrNotification
import com.achub.hram.ble.models.HramBleDevice
import com.achub.hram.identifier
import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.State
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import kotlin.test.assertIs
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

class HramHrDeviceRepoTest {
    companion object {
        private const val TEST_TIME_EPSILON_MS = 10L
        private const val ADV_INTERVAL = 950L

        private const val NOTIFICATION_INTERVAL_MS = 1000L
        private val IDENTIFIER = identifier("identifier")
    }

    private lateinit var bleConnectionManagerMock: BleConnectionManager
    private lateinit var bleDataRepoMock: BleDataRepo
    private lateinit var advertisementMock: Advertisement
    private lateinit var bleDeviceMock: HramBleDevice

    @BeforeTest
    fun setup() {
        bleConnectionManagerMock = mock()
        bleDataRepoMock = mock()
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
        val results = mutableListOf<ConnectionResult>()

        repo.connect(device = bleDeviceMock)
            .onEach(results::add)
            .launchIn(this)

        testScheduler.advanceUntilIdle()

        assertEquals(1, results.size)
        assertIs<ConnectionResult.Connected>(results[0])
        assertEquals(bleDeviceMock, (results[0] as ConnectionResult.Connected).device)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect to device - error`() = runTest {
        val exception = RuntimeException("Connection failed")
        every { bleConnectionManagerMock.connectToDevice(IDENTIFIER) } returns flow { throw exception }

        val repo = createRepo()
        val results = mutableListOf<ConnectionResult>()

        repo.connect(device = bleDeviceMock)
            .onEach(results::add)
            .launchIn(this)

        testScheduler.advanceTimeBy(1L)

        assertEquals(1, results.size)
        assertIs<ConnectionResult.Error>(results[0])
        assertEquals(exception, (results[0] as ConnectionResult.Error).error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `listen emits notification, error suppressed`() = runTest {
        val batteryLevel = 90
        val exception = NumberFormatException("Simulated battery read error")
        val peripheral = mock<Peripheral>(MockMode.autofill)
        val hrNotification = HrNotification(hrBpm = 72, isSensorContactSupported = true, isContactOn = true)
        every { bleConnectionManagerMock.onConnected } returns flowOf(peripheral)
        every { bleDataRepoMock.observeHeartRate(peripheral) } returns flowOf(hrNotification)
        every { bleDataRepoMock.observeBatteryLevel(peripheral) } returns flow {
            emit(batteryLevel)
            delay(NOTIFICATION_INTERVAL_MS)
            throw exception
        }
        every { peripheral.state } returns MutableStateFlow<State>(State.Connected(this))
        val repo = createRepo()
        val collected = mutableListOf<BleNotification>()

        repo.listen().onEach(collected::add).launchIn(this)

        advanceTimeBy(NOTIFICATION_INTERVAL_MS + TEST_TIME_EPSILON_MS)

        assertEquals(
            BleNotification(hrNotification = hrNotification, batteryLevel = batteryLevel, isBleConnected = true),
            collected.first()
        )
        assertEquals(1, collected.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `scan hr devices flow`() = runTest {
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { infiniteAdvertisement() }
        val repo = createRepo()
        val results = mutableListOf<ScanResult>()

        repo.scan((BLE_SCAN_DURATION).toDuration(DurationUnit.MILLISECONDS))
            .onEach(results::add)
            .launchIn(this)

        // Advance time to trigger timeout
        advanceTimeBy(BLE_SCAN_DURATION + TEST_TIME_EPSILON_MS)

        val scanUpdateCount = results.count { it is ScanResult.ScanUpdate }
        assertEquals(5, scanUpdateCount, "Expected 5 ScanUpdate results, got $scanUpdateCount")
        assertIs<ScanResult.Complete>(results.last(), "Last result should be Complete. Actual results: $results")
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
        val results = mutableListOf<ScanResult>()

        repo.scan(BLE_SCAN_DURATION.toDuration(DurationUnit.MILLISECONDS))
            .onEach(results::add)
            .launchIn(this)

        advanceUntilIdle()

        val hasScanUpdate = results.any { it is ScanResult.ScanUpdate }
        assertEquals(true, hasScanUpdate)
        assertIs<ScanResult.Error>(results.last())
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
        bleDataRepoMock,
        StandardTestDispatcher(testScheduler),
        bleConnectionManagerMock
    )
}
