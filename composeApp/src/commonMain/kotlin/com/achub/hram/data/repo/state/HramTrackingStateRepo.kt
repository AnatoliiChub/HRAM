package com.achub.hram.data.repo.state

import androidx.datastore.core.DataStore
import com.achub.hram.tracking.TrackingStateStage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

class HramTrackingStateRepo(val dataStore: DataStore<TrackingStateStage>) : TrackingStateRepo {
    override suspend fun update(state: TrackingStateStage) {
        dataStore.updateData { state }
    }

    override fun listen(): Flow<TrackingStateStage> = dataStore.data.filterNotNull()

    override suspend fun get(): TrackingStateStage = dataStore.data.firstOrNull()
        ?: TrackingStateStage.TRACKING_INIT_STATE

    override suspend fun release() {
        dataStore.updateData { TrackingStateStage.TRACKING_INIT_STATE }
    }
}
