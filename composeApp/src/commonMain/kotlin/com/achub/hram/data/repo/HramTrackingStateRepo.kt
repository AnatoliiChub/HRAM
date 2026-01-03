package com.achub.hram.data.repo

import androidx.datastore.core.DataStore
import com.achub.hram.data.models.TrackingState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

class HramTrackingStateRepo(val dataStore: DataStore<TrackingState>) : TrackingStateRepo {
    override suspend fun updateTrackingState(trackingState: TrackingState) {
        dataStore.updateData { trackingState }
    }

    override fun listenTrackingState(): Flow<TrackingState> = dataStore.data.filterNotNull().onEach {
        Napier.d { "Tracking state updated: $it" }
    }

    override suspend fun getTrackingStateOnce(): TrackingState = dataStore.data.firstOrNull()
        ?: TrackingState.Disconnected

    override suspend fun release() {
        dataStore.updateData { TrackingState.Disconnected }
    }
}
