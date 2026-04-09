package com.achub.hram.ble

import com.achub.hram.Logger
import com.sun.jna.Callback
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

private fun interface NotifCallback : Callback {
    fun invoke(self: Pointer, cmd: Pointer, notification: Pointer)
}

class BluetoothObserverMac : BluetoothObserver {
    override fun init() {
        NativeLibrary.getInstance("IOBluetooth")
    }

    override fun observeBleState(): Flow<Boolean> = callbackFlow {
        val objc = NativeLibrary.getInstance("objc")
        val msgSend = objc.getFunction("objc_msgSend")

        fun getClass(name: String): Pointer = objc.getFunction("objc_getClass").invokePointer(arrayOf(name))

        fun getSel(name: String): Pointer = objc.getFunction("sel_registerName").invokePointer(arrayOf(name))

        fun nsString(value: String): Pointer = msgSend.invokePointer(
            arrayOf(getClass("NSString"), getSel("stringWithUTF8String:"), value)
        )

        val controller = msgSend.invokePointer(
            arrayOf(getClass("IOBluetoothHostController"), getSel("defaultController"))
        )
        val powerStateSel = getSel("powerState")

        fun readPowerState() = (msgSend.invokeInt(arrayOf(controller, powerStateSel)) and 0xFF) == POWER_STATE_ON

        trySendBlocking(readPowerState())
            .onFailure { Logger.e(TAG) { "BluetoothState initial emit failed: $it" } }

        // ObjC classes are permanent per-process; unique name avoids re-registration crash
        val observerClass = objc.getFunction("objc_allocateClassPair").invokePointer(
            arrayOf(getClass("NSObject"), "HRAMBTObserver_${System.nanoTime()}", 0)
        )
        val onSel = getSel("handleBluetoothOn:")
        val offSel = getSel("handleBluetoothOff:")

        val onCallback = NotifCallback { _, _, _ ->
            trySendBlocking(true).onFailure { Logger.e(TAG) { "BluetoothState ON emit failed: $it" } }
        }
        val offCallback = NotifCallback { _, _, _ ->
            trySendBlocking(false).onFailure { Logger.e(TAG) { "BluetoothState OFF emit failed: $it" } }
        }

        val addMethod = objc.getFunction("class_addMethod")
        addMethod.invoke(arrayOf(observerClass, onSel, onCallback, "v@:@"))
        addMethod.invoke(arrayOf(observerClass, offSel, offCallback, "v@:@"))
        objc.getFunction("objc_registerClassPair").invoke(arrayOf(observerClass))

        val observer = msgSend.invokePointer(
            arrayOf(msgSend.invokePointer(arrayOf(observerClass, getSel("alloc"))), getSel("init"))
        )

        val notifCenter = msgSend.invokePointer(
            arrayOf(getClass("NSNotificationCenter"), getSel("defaultCenter"))
        )
        val addObserverSel = getSel("addObserver:selector:name:object:")
        msgSend.invoke(
            arrayOf(
                notifCenter,
                addObserverSel,
                observer,
                onSel,
                nsString("IOBluetoothHostControllerPoweredOnNotification"),
                Pointer.NULL
            )
        )
        msgSend.invoke(
            arrayOf(
                notifCenter,
                addObserverSel,
                observer,
                offSel,
                nsString("IOBluetoothHostControllerPoweredOffNotification"),
                Pointer.NULL
            )
        )

        awaitClose {
            msgSend.invoke(arrayOf(notifCenter, getSel("removeObserver:"), observer))
            Logger.d(TAG) { "BluetoothState observer removed" }
            check(onCallback != offCallback) // keep callbacks alive until deregistered
        }
    }.buffer(CONFLATED)
}
