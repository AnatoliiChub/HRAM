package com.achub.hram.ble.core

import com.achub.hram.ble.models.HrNotification
import com.juul.kable.Peripheral
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifyNoMoreCalls
import dev.mokkery.verifySuspend
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HramBleDataRepoTest {
    companion object {
        val INPUT_BYTES = byteArrayOf(99, 99)
        val BATTERY_LEVEL_DELAY = 1000L
        val MINIMAL_EPSILON = 10L
    }

    lateinit var parserMock: BleParser
    lateinit var peripheralMock: Peripheral
    lateinit var repo: HramBleDataRepo

    @BeforeTest
    fun setup() {
        parserMock = mock(MockMode.autofill)
        peripheralMock = mock(MockMode.autofill)
        repo = HramBleDataRepo(parserMock)
    }

    @Test
    fun `observeHeartRate maps values via parser`() = runTest {
        val hrNotification = HrNotification(60, isSensorContactSupported = false, isContactOn = false)
        val hrFlow = flowOf(INPUT_BYTES)

        every { peripheralMock.observe(HR_CHAR, any()) } returns hrFlow
        every { parserMock.parseHrNotification(INPUT_BYTES) } returns hrNotification

        val collected = mutableListOf<HrNotification>()
        val job = repo.observeHeartRate(peripheralMock).onEach { collected.add(it) }.launchIn(this)
        advanceUntilIdle()

        verify { parserMock.parseHrNotification(INPUT_BYTES) }
        assertEquals(1, collected.size)
        assertEquals(hrNotification, collected.first())

        job.cancel()
    }

    @Test
    fun `observeBatteryLevel emits read then observe and maps via parser`() = runTest {
        val batteryLevelBytesRead = byteArrayOf(50)
        val batteryLevelBytesObserved = byteArrayOf(75)
        val batteryLevelRead = 50
        val batteryLevelObserved = 75
        val batteryFlow = flow {
            delay(BATTERY_LEVEL_DELAY)
            emit(batteryLevelBytesObserved)
        }

        everySuspend { peripheralMock.read(BATTERY_CHAR) } returns batteryLevelBytesRead
        every { peripheralMock.observe(BATTERY_CHAR, any()) } returns batteryFlow
        every { parserMock.parseBatteryLevel(batteryLevelBytesRead) } returns batteryLevelRead
        every { parserMock.parseBatteryLevel(batteryLevelBytesObserved) } returns batteryLevelObserved

        val collected = mutableListOf<Int>()
        val job = repo.observeBatteryLevel(peripheralMock).onEach { collected.add(it) }.launchIn(this)
        advanceTimeBy(MINIMAL_EPSILON)

        verifySuspend { peripheralMock.read(BATTERY_CHAR) }
        verify { parserMock.parseBatteryLevel(batteryLevelBytesRead) }
        verify(VerifyMode.not) { parserMock.parseBatteryLevel(batteryLevelBytesObserved) }

        assertEquals(1, collected.size)
        assertEquals(batteryLevelRead, collected[0])

        advanceTimeBy(BATTERY_LEVEL_DELAY)
        assertEquals(2, collected.size)
        verify { parserMock.parseBatteryLevel(batteryLevelBytesObserved) }
        assertEquals(batteryLevelObserved, collected[1])

        verifyNoMoreCalls(parserMock)

        job.cancel()
    }
}
