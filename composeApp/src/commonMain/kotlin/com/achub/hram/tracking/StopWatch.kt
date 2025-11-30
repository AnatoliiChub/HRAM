package com.achub.hram.tracking

import kotlinx.coroutines.flow.Flow

interface StopWatch {
    fun start(): Unit

    fun pause(): Unit

    fun reset(): Unit

    fun listen(): Flow<Long>

    fun elapsedTimeSeconds(): Long
}
