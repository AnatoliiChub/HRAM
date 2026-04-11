package com.achub.hram.ble

import com.achub.hram.NoCoverage
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer

@NoCoverage
class RealObjcBridge(
    private val objcProvider: (String) -> NativeLibrary,
) : ObjcBridge {
    private val objc by lazy { objcProvider("objc") }
    private val msgSend by lazy { objc.getFunction("objc_msgSend") }

    override fun getClass(name: String): Pointer =
        objc.getFunction("objc_getClass").invokePointer(arrayOf(name))

    override fun getSel(name: String): Pointer =
        objc.getFunction("sel_registerName").invokePointer(arrayOf(name))

    override fun nsString(value: String): Pointer =
        msgSend.invokePointer(arrayOf(getClass("NSString"), getSel("stringWithUTF8String:"), value))

    override fun invokePointer(functionName: String, args: Array<Any?>): Pointer =
        objc.getFunction(functionName).invokePointer(args)

    override fun invokeInt(functionName: String, args: Array<Any?>): Int =
        objc.getFunction(functionName).invokeInt(args)
}

