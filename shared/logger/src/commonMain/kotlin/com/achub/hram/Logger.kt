package com.achub.hram

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object Logger {

    fun D(tag: String?, message: () -> String) {
        Napier.d(tag = tag) { message() }
    }

    fun E(tag: String?, message: () -> String) {
        Napier.e(tag = tag) { message() }
    }

    /** Call once at app startup (inside initKoin) to attach the Napier debug antilog. */
    fun init() {
        Napier.base(DebugAntilog())
    }
}
