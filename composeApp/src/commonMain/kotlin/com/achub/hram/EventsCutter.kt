package com.achub.hram

import kotlin.time.Clock.System.now
import kotlin.time.ExperimentalTime

class EventsCutter(val debounceInterval: Long) {
    companion object

    @OptIn(ExperimentalTime::class)
    private val now: Long
        get() = now().toEpochMilliseconds()

    private var lastEventTimeMs: Long = 0

    fun processEvent(event: () -> Unit) {
        if (now - lastEventTimeMs >= debounceInterval) {
            event.invoke()
            lastEventTimeMs = now
        }
    }
}

fun EventsCutter.Companion.get(debounceInterval: Long) = EventsCutter(debounceInterval)
