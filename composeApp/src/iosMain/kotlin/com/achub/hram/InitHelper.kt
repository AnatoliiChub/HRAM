package com.achub.hram

import com.achub.hram.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

fun init() {
    initKoin()
    Napier.base(DebugAntilog())
}
