package com.achub.hram.ble

import com.sun.jna.Pointer

/**
 * Thin abstraction over Objective-C / JNA runtime operations used by
 * [BluetoothObserverMac].
 *
 * The production implementation delegates to JNA (`RealObjcBridge`). The interface intentionally
 * exposes a very small surface necessary for the observer implementation.
 */
interface ObjcBridge {
    /**
     * Return a pointer to the Objective-C class with the given name (objc_getClass).
     * Example: `objcGetClass("NSString")`.
     */
    fun getClass(name: String): Pointer

    /**
     * Return a selector pointer for the given selector name (sel_registerName).
     * Example: `getSel("defaultController")`.
     */
    fun getSel(name: String): Pointer

    /**
     * Convenience for creating an `NSString` pointer from a Kotlin String.
     * Implementations typically call `[NSString stringWithUTF8String:]` via
     * objc_msgSend and return the resulting object pointer.
     */
    fun nsString(value: String): Pointer

    /**
     * Invoke a native function that returns a pointer. The canonical caller is
     * the Objective-C runtime function `objc_msgSend`, but this method is
     * generic so other functions (e.g. class_addMethod) can be invoked as well.
     *
     * @param functionName native symbol name (e.g. "objc_msgSend")
     * @param args arguments to pass to the native function
     * @return a native pointer result
     */
    fun invokePointer(functionName: String, args: Array<Any?>): Pointer

    /**
     * Invoke a native function that returns an integer (e.g. reading a numeric
     * property via `objc_msgSend`). This is used to read the Bluetooth power
     * state from the controller in `BluetoothObserverMac`.
     *
     * @param functionName native symbol name (e.g. "objc_msgSend")
     * @param args arguments to pass to the native function
     * @return integer result returned by the native call
     */
    fun invokeInt(functionName: String, args: Array<Any?>): Int
}
