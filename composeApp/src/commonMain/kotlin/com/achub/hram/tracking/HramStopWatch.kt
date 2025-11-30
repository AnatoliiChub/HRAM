package com.achub.hram.tracking

import com.achub.hram.ext.tickerFlow
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
class HramStopWatch : StopWatch {
    companion object {
        private val STOP_WATCH_TICK_DURATION = STOP_WATCH_TICK_MS.toDuration(DurationUnit.MILLISECONDS)
    }

    private var isRunning: Boolean? = null

    private val elapsedTime: Channel<Long> = Channel()

    private var startedTimestamp: Long = 0

    private val accumulatedOnLastPaused = AtomicLong(0L)

    override fun start() {
        startedTimestamp = now().toEpochMilliseconds()
        isRunning = true
    }

    override fun pause() {
        accumulatedOnLastPaused.update { it + now().toEpochMilliseconds() - startedTimestamp }
        isRunning = false
    }

    override fun reset() {
        accumulatedOnLastPaused.update { 0L }
        isRunning = null
    }

    override fun listen() = tickerFlow(STOP_WATCH_TICK_DURATION)
        .map { elapsedTimeSeconds() }
        .distinctUntilChanged()
        .onEach { elapsedTime.trySend(it) }

    override fun elapsedTimeSeconds(): Long = when (isRunning) {
        true -> accumulatedOnLastPaused.load() + (now().toEpochMilliseconds() - startedTimestamp)
        false -> accumulatedOnLastPaused.load()
        null -> 0
    }.toDuration(DurationUnit.MILLISECONDS).inWholeSeconds
}
