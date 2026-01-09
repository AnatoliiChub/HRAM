package com.achub.hram.data.repo.state

import androidx.datastore.core.DataStore
import com.achub.hram.ext.logger
import com.achub.hram.tracking.TrackingStateStage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

private val TAG = "HramTrackingStateStageRepo"

class HramTrackingStateRepo(val dataStore: DataStore<TrackingStateStage>) : TrackingStateRepo {
    override suspend fun update(state: TrackingStateStage) {
        logger(TAG) { "Updating tracking state stage to: $state" }
        dataStore.updateData { state }
    }

    override fun listen(): Flow<TrackingStateStage> = dataStore.data.filterNotNull().onEach {
        Napier.d { "Tracking state stage updated: $it" }
    }

    override suspend fun get(): TrackingStateStage = dataStore.data.firstOrNull()
        ?: TrackingStateStage.TRACKING_INIT_STATE

    override suspend fun release() {
        dataStore.updateData { TrackingStateStage.TRACKING_INIT_STATE }
    }
}
