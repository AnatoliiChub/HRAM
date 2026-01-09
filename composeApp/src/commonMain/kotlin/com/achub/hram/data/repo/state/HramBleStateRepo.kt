package com.achub.hram.data.repo.state

import androidx.datastore.core.DataStore
import com.achub.hram.data.models.BleState
import com.achub.hram.ext.logger
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

private val TAG = "HramBleStateRepo"

class HramBleStateRepo(val dataStore: DataStore<BleState>) : BleStateRepo {
    override suspend fun update(state: BleState) {
        logger(TAG) { "Updating ble state to: $state" }
        dataStore.updateData { state }
    }

    override fun listen(): Flow<BleState> = dataStore.data.filterNotNull().onEach {
        Napier.d { "ble state updated: $it" }
    }

    override suspend fun get(): BleState = dataStore.data.firstOrNull()
        ?: BleState.Disconnected

    override suspend fun release() {
        dataStore.updateData { BleState.Disconnected }
    }
}
