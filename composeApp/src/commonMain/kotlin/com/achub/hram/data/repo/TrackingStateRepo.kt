package com.achub.hram.data.repo

import com.achub.hram.data.models.BleState
import kotlinx.coroutines.flow.Flow

interface TrackingStateRepo {
    suspend fun updateTrackingState(bleState: BleState)

    fun listenTrackingState(): Flow<BleState>

    suspend fun getTrackingStateOnce(): BleState

    suspend fun release()
}
