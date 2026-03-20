package com.achub.hram.ble.utils

import io.github.aakira.napier.Napier
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlin.time.Duration

fun logger(tag: String?, message: () -> String) {
    Napier.d { "[$tag] ${message()}" }
}

fun loggerE(tag: String?, message: () -> String) {
    Napier.e { "[$tag] ${message()}" }
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.cancelAfter(duration: Duration) = this.combine(
    flow {
        emit(Unit)
        delay(duration.inWholeMilliseconds + 1)
    }.timeout(duration)
) { result, _ -> result }
