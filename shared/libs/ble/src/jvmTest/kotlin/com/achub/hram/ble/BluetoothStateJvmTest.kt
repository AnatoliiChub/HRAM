package com.achub.hram.ble

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BluetoothStateJvmTest {
    @Test
    fun `isBluetoothOn exposes observer values`() = runTest {
        val emittedValues = listOf(false, true, true)
        val observer = mock<BluetoothObserver>()
        every { observer.observeBleState() } returns flow {
            emittedValues.forEach { emit(it) }
        }

        val state = BluetoothStateJvm(observer)

        val values = state.isBluetoothOn.take(emittedValues.size).toList()
        assertEquals(emittedValues, values)
    }
}
