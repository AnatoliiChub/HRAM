package com.achub.hram.ble.core.connection

import com.achub.hram.ble.identifier
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
        val targetId = identifier("target-id")
        val otherId = identifier("other-id")
        every { targetAdvertisementMock.identifier } returns targetId
        every { otherAdvertisementMock.identifier } returns otherId
        scanner = HramBleScanner(kableScannerMock)
    }

    @Test
    fun `scan flow returns all advertisements`() = runTest {
        every { kableScannerMock.advertisements } returns flowOf(targetAdvertisementMock, otherAdvertisementMock)

        val results = mutableListOf<Advertisement>()
        scanner.scan().collect { results.add(it) }

        assertEquals(2, results.size)
        assertEquals(targetAdvertisementMock, results[0])
        assertEquals(otherAdvertisementMock, results[1])
    }

    @Test
    fun `scan by identifier returns matching advertisement`() = runTest {
        every { kableScannerMock.advertisements } returns flowOf(otherAdvertisementMock, targetAdvertisementMock)

        val result = scanner.scan(identifier("target-id"), 5.seconds)

        assertEquals(targetAdvertisementMock, result)
    }

    @Test
    fun `scan by identifier throws timeout when not found`() = runTest {
        every { kableScannerMock.advertisements } returns flow {
            emit(otherAdvertisementMock)
            delay(2000)
        }

        assertFailsWith<TimeoutCancellationException> {
            scanner.scan(identifier("target-id"), 100.milliseconds)
        }
    }
}
