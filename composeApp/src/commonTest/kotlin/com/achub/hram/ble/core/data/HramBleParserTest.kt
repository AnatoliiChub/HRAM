package com.achub.hram.ble.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HramBleParserTest {
    private val cases = listOf(
        HrTestCase(
            name = "8-bit, no contact support",
            data = byteArrayOf(0x00.toByte(), 75.toByte()),
            expectedHr = 75,
            expectedSupported = false,
            expectedContact = true
        ),
        HrTestCase(
            name = "8-bit, contact supported and detected",
            data = byteArrayOf(0x06.toByte(), 80.toByte()),
            expectedHr = 80,
            expectedSupported = true,
            expectedContact = true
        ),
        HrTestCase(
            name = "8-bit, contact supported but not detected",
            data = byteArrayOf(0x04.toByte(), 0.toByte()),
            expectedHr = 0,
            expectedSupported = true,
            expectedContact = false
        ),
        HrTestCase(
            name = "16-bit, contact supported and detected",
            data = byteArrayOf(0x07.toByte(), 0x4c.toByte(), 0x00.toByte()),
            expectedHr = 76,
            expectedSupported = true,
            expectedContact = true
        )
    )

    private val parser = HramBleParser()

    @Test
    fun `parse heart rate notification`() {
        for (case in cases) {
            val result = parser.parseHrNotification(case.data)
            assertEquals(case.expectedHr, result.hrBpm, "case: ${case.name}")
            assertEquals(case.expectedSupported, result.isSensorContactSupported, "case: ${case.name}")
            if (case.expectedContact) {
                assertTrue(result.isContactOn, "case: ${case.name}")
            } else {
                assertFalse(result.isContactOn, "case: ${case.name}")
            }
        }
    }

    @Test
    fun `parse battery level`() {
        val battery = byteArrayOf(42.toByte())
        // preserved original expected value
        assertEquals(42, parser.parseBatteryLevel(battery))
    }
}

data class HrTestCase(
    val name: String,
    val data: ByteArray,
    val expectedHr: Int,
    val expectedSupported: Boolean,
    val expectedContact: Boolean
)
