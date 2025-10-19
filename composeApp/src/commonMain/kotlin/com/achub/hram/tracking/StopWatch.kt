package com.achub.hram.tracking

import com.achub.hram.tickerFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import org.koin.core.annotation.Single
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update
import kotlin.time.Clock.System.now
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

private const val STOP_WATCH_TICK_MS = 100L

@OptIn(ExperimentalTime::class, ExperimentalAtomicApi::class, ExperimentalCoroutinesApi::class)
@Single
class StopWatch {

    companion object {
        private val STOP_WATCH_TICK_DURATION = STOP_WATCH_TICK_MS.toDuration(DurationUnit.MILLISECONDS)
    }

    private val isRunning = Channel<Boolean?>()

    private var startedTimestamp: Long = 0

    private val accumulatedOnLastPaused = AtomicLong(0L)

    fun start() {
        startedTimestamp = now().toEpochMilliseconds()
        isRunning.trySend(true)
    }

    fun pause() {
        accumulatedOnLastPaused.update { it + now().toEpochMilliseconds() - startedTimestamp }
        isRunning.trySend(false)

    }

    fun  reset() {
        accumulatedOnLastPaused.update { 0L }
        isRunning.trySend(null)
    }

    fun listen() = isRunning.receiveAsFlow()
        .flatMapLatest {
            if (it == null) flow { emit(0) }
            else if (it) tickerFlow(STOP_WATCH_TICK_DURATION)
                .map { accumulatedOnLastPaused.load() + (now().toEpochMilliseconds() - startedTimestamp) }
            else flow { emit(accumulatedOnLastPaused.load()) }
        }
        .map { it.toDuration(DurationUnit.MILLISECONDS).inWholeSeconds }
        .distinctUntilChanged()

}