package com.achub.hram.ble

import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.model.BleDevice
import com.achub.hram.ble.model.BleNotification
import com.achub.hram.ble.model.HrNotification
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HramHrDeviceRepoTest {
    companion object {
        private const val TEST_TIME_EPSILON_MS = 10L
        private const val ADV_INTERVAL = 950L
    }

    private lateinit var bleConnectionManagerMock: BleConnectionManager
    private lateinit var bleDataRepoMock: BleDataRepo

    @BeforeTest
    fun setup() {
        bleConnectionManagerMock = mock()
        bleDataRepoMock = mock()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `scan hr devices flow`() = runTest {
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { infiniteAdvertisement() }

        val repo = createRepo()

        val initCalled = mock<Runnable>(MockMode.autofill)
        val completeCalled = mock<Runnable>(MockMode.autofill)
        val updateCalled = mock<Runnable>(MockMode.autofill)

        repo.scan(onInit = initCalled::run, onUpdate = { updateCalled.run() }, onComplete = completeCalled::run)

        advanceTimeBy(TEST_TIME_EPSILON_MS)

        verify { initCalled.run() }
        verify(VerifyMode.not) { updateCalled.run() }
        verify(VerifyMode.not) { completeCalled.run() }

        advanceTimeBy(SCAN_DURATION)

        verify(VerifyMode.exactly(5)) { updateCalled.run() }
        verify { completeCalled.run() }

        advanceTimeBy(SCAN_DURATION)
        verifyNoMoreCalls(initCalled, updateCalled, completeCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect with no matching identifier does nothing`() = runTest {
        val adv = mock<Advertisement>(MockMode.autofill)
        every { adv.identifier } returns "identifier1" as Identifier
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { emit(adv) }

        val repo = createRepo()

        val initCalled = mock<Runnable>(MockMode.autofill)
        val connectedCalled = mock<Runnable>(MockMode.autofill)

        val target = BleDevice(name = "NoAdv", identifier = "NO MATCHING IDENTIFIER")
        repo.connect(device = target, onInitConnection = initCalled::run, onConnected = { _ -> connectedCalled.run() })

        testScheduler.advanceUntilIdle()

        verify(VerifyMode.not) { initCalled.run() }
        verify(VerifyMode.not) { connectedCalled.run() }
        verifyNoMoreCalls(initCalled, connectedCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connect with matching identifier`() = runTest {
        val advertisement = mock<Advertisement>(MockMode.autofill)
        every { advertisement.identifier } returns "identifier" as Identifier
        every { bleConnectionManagerMock.scanHrDevices() } returns flow { emit(advertisement) }

        val target = BleDevice(name = "Adv", identifier = "identifier")
        every { bleConnectionManagerMock.connectToDevice(advertisement) } returns flow { emit(target) }

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
    fun `disconnect during scanning`() = runTest {
        everySuspend { bleConnectionManagerMock.disconnect() } returns Unit

        every { bleConnectionManagerMock.scanHrDevices() } returns flow { infiniteAdvertisement() }

        val repo = createRepo()

        val initCalled = mock<Runnable>(MockMode.autofill)
        val completeCalled = mock<Runnable>(MockMode.autofill)
        val updateCalled = mock<Runnable>(MockMode.autofill)

        repo.scan(onInit = initCalled::run, onUpdate = { updateCalled.run() }, onComplete = completeCalled::run)

        advanceTimeBy(ADV_INTERVAL + TEST_TIME_EPSILON_MS)
        verify { initCalled.run() }
        verify { updateCalled.run() }

        repo.disconnect()
        advanceTimeBy(TEST_TIME_EPSILON_MS)

        verify { completeCalled.run() }
        verifySuspend { bleConnectionManagerMock.disconnect() }
        advanceUntilIdle()
        verifyNoMoreCalls(initCalled, updateCalled, completeCalled)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun FlowCollector<Advertisement>.infiniteAdvertisement() {
        while (true) {
            val adv = mock<Advertisement>(MockMode.autofill)
            every { adv.peripheralName } returns "HRM Device${Clock.System.now().nanosecondsOfSecond}"
            every { adv.identifier } returns "identifier-${Clock.System.now().nanosecondsOfSecond}" as Identifier
            delay(ADV_INTERVAL)
            emit(adv)
        }
    }

    private fun TestScope.createRepo() = HramHrDeviceRepo(
        this,
        bleDataRepoMock,
        bleConnectionManagerMock,
        StandardTestDispatcher(testScheduler)
    )
}
