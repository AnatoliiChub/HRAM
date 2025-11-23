package com.achub.hram.tracking

import com.achub.hram.tickerFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.time.Clock.System.now
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private const val STOP_WATCH_TICK_MS = 100L

@OptIn(ExperimentalTime::class, ExperimentalAtomicApi::class, ExperimentalCoroutinesApi::class)
class StopWatch {

    companion object {
        private val STOP_WATCH_TICK_DURATION = STOP_WATCH_TICK_MS.toDuration(DurationUnit.MILLISECONDS)
    }

    private var isRunning: Boolean? = null

    val elapsedTime: Channel<Long> = Channel()

    private var startedTimestamp: Long = 0

    private val accumulatedOnLastPaused = AtomicLong(0L)

    fun start() {
        startedTimestamp = now().toEpochMilliseconds()
        isRunning = true
    }

    fun pause() {
        accumulatedOnLastPaused.update { it + now().toEpochMilliseconds() - startedTimestamp }
        isRunning = false

    }

    fun reset() {
        accumulatedOnLastPaused.update { 0L }
        isRunning = null
    }

    fun listen() = tickerFlow(STOP_WATCH_TICK_DURATION)
        .map { elapsedTimeSeconds() }
        .distinctUntilChanged()
        .onEach { elapsedTime.trySend(it) }

    fun elapsedTimeSeconds(): Long = when (isRunning) {
        true -> accumulatedOnLastPaused.load() + (now().toEpochMilliseconds() - startedTimestamp)
        false -> accumulatedOnLastPaused.load()
        null -> 0
    }.toDuration(DurationUnit.MILLISECONDS).inWholeSeconds

}