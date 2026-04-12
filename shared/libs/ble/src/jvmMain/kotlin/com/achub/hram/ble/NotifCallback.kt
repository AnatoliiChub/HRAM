package com.achub.hram.ble

import com.sun.jna.Callback
import com.sun.jna.Pointer

fun interface NotifCallback : Callback {
    fun invoke(self: Pointer, cmd: Pointer, notification: Pointer)
}
