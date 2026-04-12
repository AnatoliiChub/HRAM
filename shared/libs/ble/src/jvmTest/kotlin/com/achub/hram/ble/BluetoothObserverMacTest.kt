package com.achub.hram.ble

import com.sun.jna.Pointer
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BluetoothObserverMacTest {
    @Test
    fun `initial power state is emitted`() = runTest {
        val bridge = mock<ObjcBridge>()

        // Return pointers for class/selector lookups used during setup
        every { bridge.getClass(any()) } returns Pointer(0)
        every { bridge.getSel(any()) } returns Pointer(0)
        // Simulate power ON for initial controller powerState call
        every { bridge.invokeInt("objc_msgSend", any()) } returns 1
        every { bridge.invokePointer(any(), any()) } returns Pointer(0)
        every { bridge.nsString(any()) } returns Pointer(0)

        val observer = BluetoothObserverMac(bridge = bridge)
        val first = observer.observeBleState().first()
        assertTrue(first)
    }

    @Test
    fun `registers observer methods with Objc runtime`() = runTest {
        val bridge = mock<ObjcBridge>()

        every { bridge.getClass(any()) } returns Pointer(0)
        every { bridge.getSel(any()) } returns Pointer(0)
        every { bridge.nsString(any()) } returns Pointer(0)
        every { bridge.invokePointer(any(), any()) } returns Pointer(0)
        every { bridge.invokeInt(any(), any()) } returns 0

        val observer = BluetoothObserverMac(bridge = bridge)

        observer.observeBleState().first()

        // Verify that class_addMethod and objc_registerClassPair were called (registration path)
        verify { bridge.invokePointer("class_addMethod", any()) }
        verify { bridge.invokePointer("objc_registerClassPair", any()) }
    }
}
