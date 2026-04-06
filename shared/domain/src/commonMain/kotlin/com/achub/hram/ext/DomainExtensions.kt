package com.achub.hram.ext

import com.achub.hram.domain.model.ActivityRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun <T> Flow<T>.launchIn(scope: CoroutineScope) = scope.launch { collect() }

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}

fun MutableList<Job>.cancelAndClear() {
    this.forEach { it.cancel() }
    this.clear()
}

@OptIn(ExperimentalUuidApi::class)
fun createActivity(name: String, currentTime: Long): ActivityRecord {
    val activity = ActivityRecord(
        Uuid.random().toString() + currentTime,
        name,
        0L,
        currentTime
    )
    return activity
}

@OptIn(FlowPreview::class)
fun <T> Flow<T>.cancelAfter(duration: Duration) = this.combine(
    flow {
        emit(Unit)
        delay(duration.inWholeMilliseconds + 1)
    }.timeout(duration)
) { result, _ -> result }

