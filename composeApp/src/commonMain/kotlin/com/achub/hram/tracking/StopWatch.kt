package com.achub.hram.tracking

import kotlinx.coroutines.flow.Flow

interface StopWatch {
    fun start()

    fun pause()

    fun reset()

    fun listen(): Flow<Long>

    fun elapsedTimeSeconds(): Long
}
