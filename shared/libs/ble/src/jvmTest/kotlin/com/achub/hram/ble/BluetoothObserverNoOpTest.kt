package com.achub.hram.ble

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BluetoothObserverNoOpTest {
    @Test
    fun `observeBleState emits true immediately`() = runTest {
        val observer = BluetoothObserverNoOp()
        val first = observer.observeBleState().first()
        assertTrue(first, "BluetoothObserverNoOp should emit true as the initial state")
    }
}

