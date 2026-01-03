package com.achub.hram.data.repo

import androidx.datastore.core.DataStore
import com.achub.hram.data.models.BleState
import com.achub.hram.ext.logger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

private val TAG = "HramTrackingStateRepo"

class HramTrackingStateRepo(val dataStore: DataStore<BleState>) : TrackingStateRepo {
    override suspend fun updateTrackingState(bleState: BleState) {
        logger(TAG) { "Updating tracking state to: $bleState" }
        dataStore.updateData { bleState }
    }

    override fun listenTrackingState(): Flow<BleState> = dataStore.data.filterNotNull().onEach {
        Napier.d { "Tracking state updated: $it" }
    }

    override suspend fun getTrackingStateOnce(): BleState = dataStore.data.firstOrNull()
        ?: BleState.Disconnected

    override suspend fun release() {
        dataStore.updateData { BleState.Disconnected }
    }
}
