package com.achub.hram.ble

import com.achub.hram.Logger
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "BluetoothObserverMac"
private const val POWER_STATE_ON = 1
private const val BYTE_MASK = 0xFF

/**
 * BluetoothObserverMac now depends on an ObjcBridge. By default, it uses RealObjcBridge(), so
 * production behavior is unchanged. Tests can inject a fake bridge.
 */
class BluetoothObserverMac(
    private val bridge: ObjcBridge = RealObjcBridge({ NativeLibrary.getInstance("IOBluetooth") })
) : BluetoothObserver {
    // Extracted helpers to class-level private functions to make the flow easier to read

    private fun objcGetClass(name: String): Pointer = bridge.getClass(name)

    private fun objcGetSelector(name: String): Pointer = bridge.getSel(name)

    private fun objcNsString(value: String): Pointer = bridge.nsString(value)

    private fun readPowerState(controller: Pointer, powerStateSelector: Pointer): Boolean {
        return (bridge.invokeInt("objc_msgSend", arrayOf(controller, powerStateSelector)) and BYTE_MASK) == POWER_STATE_ON
    }

    // Allocates and registers a temporary Objective-C observer class and returns its Pointer
    private fun allocateObserverClass(): Pointer = bridge.invokePointer(
        "objc_allocateClassPair",
        arrayOf(objcGetClass("NSObject"), "HRAMBTObserver_${System.nanoTime()}", 0)
    )

    private fun addMethodToClass(observerClass: Pointer, selector: Pointer, callback: NotifCallback) {
        bridge.invokePointer("class_addMethod", arrayOf(observerClass, selector, callback, "v@:@"))
    }

    private fun registerClassPair(observerClass: Pointer) {
        bridge.invokePointer("objc_registerClassPair", arrayOf(observerClass))
    }

    private fun createObserverInstance(observerClass: Pointer): Pointer = bridge.invokePointer(
        "objc_msgSend",
        arrayOf(
            bridge.invokePointer("objc_msgSend", arrayOf(observerClass, objcGetSelector("alloc"))),
            objcGetSelector("init")
        )
    )

    private fun getNotificationCenter(): Pointer = bridge.invokePointer(
        "objc_msgSend",
        arrayOf(objcGetClass("NSNotificationCenter"), objcGetSelector("defaultCenter"))
    )

    private fun addObserver(
        notificationCenter: Pointer,
        observer: Pointer,
        selector: Pointer,
        notificationName: String
    ) {
        val addObserverSelector = objcGetSelector("addObserver:selector:name:object:")
        bridge.invokePointer(
            "objc_msgSend",
            arrayOf(
                notificationCenter,
                addObserverSelector,
                observer,
                selector,
                objcNsString(notificationName),
                Pointer.NULL
            )
        )
    }

    private fun removeObserver(notificationCenter: Pointer, observer: Pointer) {
        bridge.invokePointer(
            "objc_msgSend",
            arrayOf(
                notificationCenter,
                objcGetSelector("removeObserver:"),
                observer
            )
        )
    }

    override fun observeBleState(): Flow<Boolean> = callbackFlow {
        val controller = bridge.invokePointer(
            "objc_msgSend",
            arrayOf(objcGetClass("IOBluetoothHostController"), objcGetSelector("defaultController"))
        )
        val powerStateSelector = objcGetSelector("powerState")

        trySendBlocking(readPowerState(controller, powerStateSelector))
            .onFailure { Logger.e(TAG) { "BluetoothState initial emit failed: $it" } }

        // ObjC classes are permanent per-process; unique name avoids re-registration crash
        val objcObserverClass = allocateObserverClass()

        val onSelector = objcGetSelector("handleBluetoothOn:")
        val offSelector = objcGetSelector("handleBluetoothOff:")

        val onCallback = NotifCallback { _, _, _ ->
            trySendBlocking(true).onFailure { Logger.e(TAG) { "BluetoothState ON emit failed: $it" } }
        }
        val offCallback = NotifCallback { _, _, _ ->
            trySendBlocking(false).onFailure { Logger.e(TAG) { "BluetoothState OFF emit failed: $it" } }
        }

        addMethodToClass(objcObserverClass, onSelector, onCallback)
        addMethodToClass(objcObserverClass, offSelector, offCallback)
        registerClassPair(objcObserverClass)

        val objcObserver = createObserverInstance(objcObserverClass)

        val notificationCenter = getNotificationCenter()

        addObserver(notificationCenter, objcObserver, onSelector, "IOBluetoothHostControllerPoweredOnNotification")
        addObserver(notificationCenter, objcObserver, offSelector, "IOBluetoothHostControllerPoweredOffNotification")

        awaitClose {
            removeObserver(notificationCenter, objcObserver)
            Logger.d(TAG) { "BluetoothState observer removed" }
            check(onCallback != offCallback) // keep callbacks alive until deregistered
        }
    }.buffer(CONFLATED)
}
