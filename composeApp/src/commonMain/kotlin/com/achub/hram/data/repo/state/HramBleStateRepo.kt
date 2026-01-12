package com.achub.hram.data.repo.state

import androidx.datastore.core.DataStore
import com.achub.hram.data.models.BleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

class HramBleStateRepo(val dataStore: DataStore<BleState>) : BleStateRepo {
    override suspend fun update(state: BleState) {
        dataStore.updateData { state }
    }

    override fun listen(): Flow<BleState> = dataStore.data.filterNotNull()

    override suspend fun get(): BleState = dataStore.data.firstOrNull()
        ?: BleState.Disconnected

    override suspend fun release() {
        dataStore.updateData { BleState.Disconnected }
    }
}
