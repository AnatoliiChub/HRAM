package com.achub.hram.data.repo

import com.achub.hram.data.models.TrackingState
import kotlinx.coroutines.flow.Flow

interface TrackingStateRepo {
    suspend fun updateTrackingState(trackingState: TrackingState)

    fun listenTrackingState(): Flow<TrackingState>

    suspend fun getTrackingStateOnce(): TrackingState

    suspend fun release()
}
