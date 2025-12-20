package com.achub.hram.ble.core.connection

import com.achub.hram.identifier
import com.juul.kable.Advertisement
import com.juul.kable.Scanner
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HramBleScannerTest {
    private lateinit var kableScannerMock: Scanner<Advertisement>
    private lateinit var targetAdvertisementMock: Advertisement
    private lateinit var otherAdvertisementMock: Advertisement

    private lateinit var scanner: HramBleScanner

    @BeforeTest
    fun setup() {
        kableScannerMock = mock(MockMode.autofill)
        targetAdvertisementMock = mock(MockMode.autofill)
        otherAdvertisementMock = mock(MockMode.autofill)
        scanner = HramBleScanner(kableScannerMock)
    }

    @Test
    fun `scan - returns flow from underlying scanner`() = runTest {
        val expectedFlow = flowOf(targetAdvertisementMock)
        every { kableScannerMock.advertisements } returns expectedFlow

        val result = scanner.scan()

        assertEquals(expectedFlow, result)
    }

    @Test
    fun `scan with identifier - finds matching device`() = runTest {
        val targetId = identifier("target-id")
        val otherId = identifier("other-id")
        every { targetAdvertisementMock.identifier } returns targetId
        every { otherAdvertisementMock.identifier } returns otherId
        val scanFlow = flowOf(otherAdvertisementMock, targetAdvertisementMock)
        every { kableScannerMock.advertisements } returns scanFlow

        val result = scanner.scan(targetId, 1.seconds)

        assertEquals(targetAdvertisementMock, result)
    }

    @Test
    fun `scan with identifier - throws timeout if device not found`() = runTest {
        val targetId = identifier("target-id")
        val otherId = identifier("other-id")
        every { otherAdvertisementMock.identifier } returns otherId
        val scanFlow = flow {
            emit(otherAdvertisementMock)
            delay(2.seconds) // Delay longer than timeout
        }
        every { kableScannerMock.advertisements } returns scanFlow

        assertFailsWith<TimeoutCancellationException> {
            scanner.scan(targetId, 100.milliseconds)
        }
    }
}
