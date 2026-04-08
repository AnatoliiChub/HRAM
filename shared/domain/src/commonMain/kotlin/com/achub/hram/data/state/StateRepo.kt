package com.achub.hram.data.state

import kotlinx.coroutines.flow.Flow

interface StateRepo<T> {
    suspend fun update(state: T)

    fun listen(): Flow<T>

    suspend fun get(): T

    suspend fun release()
}

