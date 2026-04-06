package com.achub.hram.tracking.stopwatch

import kotlinx.coroutines.flow.Flow

interface StopWatch {
    fun start()

    fun pause()

    fun reset()

    fun listen(): Flow<Long>

    fun elapsedTime(): Long
}
