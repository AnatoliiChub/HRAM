package com.achub.hram.ble

import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.model.BleDevice
import com.juul.kable.Advertisement
import com.juul.kable.Identifier
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class HramHrDeviceRepoTest {
    companion object {
        private const val SHORT_TIME = 100L
        private const val ADV_INTERVAL = 950L
    }

    private lateinit var bleConnectionManager: BleConnectionManager
    private lateinit var bleDataRepo: BleDataRepo

    @BeforeTest
    fun setup() {
        bleConnectionManager = mock()
        bleDataRepo = mock()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun `scan hr devices flow`() = runTest {
        every { bleConnectionManager.scanHrDevices() } returns flow { infiniteAdvertisement() }

        val repo = HramHrDeviceRepo(this, bleDataRepo, bleConnectionManager, StandardTestDispatcher(testScheduler))

        val initCalled = mock<Runnable>(MockMode.autofill)
        val completeCalled = mock<Runnable>(MockMode.autofill)
        val updateCalled = mock<Runnable>(MockMode.autofill)

        repo.scan(
            onInit = initCalled::run,
            onUpdate = { updateCalled.run() },
            onComplete = completeCalled::run,
        )

        advanceTimeBy(SHORT_TIME)

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
        every { bleConnectionManager.scanHrDevices() } returns flow { emit(adv) }

        val repo = HramHrDeviceRepo(this, bleDataRepo, bleConnectionManager, StandardTestDispatcher(testScheduler))

        val initCalled = mock<Runnable>(MockMode.autofill)
        val connectedCalled = mock<Runnable>(MockMode.autofill)

        val target = BleDevice(name = "NoAdv", identifier = "identifier")
        repo.connect(
            device = target,
            onInitConnection = initCalled::run,
            onConnected = { _ -> connectedCalled.run() }
        )

        testScheduler.advanceUntilIdle()

        verify(VerifyMode.not) { initCalled.run() }
        verify(VerifyMode.not) { connectedCalled.run() }
        verifyNoMoreCalls(initCalled, connectedCalled)
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun FlowCollector<Advertisement>.infiniteAdvertisement() {
        while (true) {
            val adv = mock<Advertisement>(MockMode.autofill)
            every { adv.peripheralName } returns "HRM Device${Clock.System.now().nanosecondsOfSecond}"
            delay(ADV_INTERVAL)
            emit(adv)
        }
    }
}
