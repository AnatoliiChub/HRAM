package com.achub.hram.ble

import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.connection.BleConnectionManager
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
    }

    private lateinit var bleConnectionManagerMock: BleConnectionManager
    private lateinit var bleDataRepoMock: BleDataRepo
    private lateinit var onScanInitMock: Runnable
    private lateinit var onScanCompleteMock: Runnable
    private lateinit var onScanUpdateMock: Runnable
    private lateinit var onScanErrorMock: (Throwable) -> Unit

    @BeforeTest
    fun setup() {
        bleConnectionManagerMock = mock()
        bleDataRepoMock = mock()
        onScanInitMock = mock(MockMode.autofill)
        onScanCompleteMock = mock(MockMode.autofill)
        onScanUpdateMock = mock(MockMode.autofill)
        onScanErrorMock = mock(MockMode.autofill)
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

        advanceTimeBy(SCAN_DURATION)

        verify(VerifyMode.exactly(5)) { onScanUpdateMock.run() }
        verify { onScanCompleteMock.run() }

        advanceTimeBy(SCAN_DURATION)
        verifyNoMoreCalls(onScanInitMock, onScanUpdateMock, onScanCompleteMock, onScanErrorMock)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect to device`() = runTest {
        val identifier = identifier("identifier")
        val advertisement = mock<Advertisement>(MockMode.autofill)
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { emit(advertisement) }
        val target = mock<BleDevice>(MockMode.autofill)
        every { target.provideIdentifier() } returns identifier
        every { bleConnectionManagerMock.connectToDevice(identifier) } returns flow { emit(target) }

        val repo = createRepo()

        val initCalled = mock<Runnable>(MockMode.autofill)
        val connectedCalled: (BleDevice) -> Unit = mock(MockMode.autofill)

        repo.connect(device = target, onInitConnection = initCalled::run, onConnected = connectedCalled::invoke)

        testScheduler.advanceUntilIdle()

        verify { initCalled.run() }
        verify(VerifyMode.exactly(1)) { connectedCalled.invoke(target) }
        verifyNoMoreCalls(initCalled, connectedCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `listen emits notification`() = runTest {
        val peripheral = mock<Peripheral>(MockMode.autofill)
        val hrNotification = HrNotification(hrBpm = 72, isSensorContactSupported = true, isContactOn = true)

        every { bleConnectionManagerMock.onConnected } returns flowOf(peripheral)
        every { bleDataRepoMock.observeHeartRate(peripheral) } returns flowOf(hrNotification)
        every { bleDataRepoMock.observeBatteryLevel(peripheral) } returns flowOf(85)
        every { peripheral.state } returns MutableStateFlow<State>(State.Connected(this))

        val repo = createRepo()

        val notification = repo.listen().first()

        assertEquals(
            BleNotification(hrNotification = hrNotification, batteryLevel = 85, isBleConnected = true),
            notification
        )
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
