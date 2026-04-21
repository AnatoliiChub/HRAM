package com.achub.hram.data.repo.state

import androidx.datastore.core.DataStore
import com.achub.hram.data.state.SettingsStateRepo
import com.achub.hram.models.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

class HramSettingsStateRepo(val dataStore: DataStore<UserSettings>) : SettingsStateRepo {
    override suspend fun update(state: UserSettings) {
        dataStore.updateData { state }
    }

    override fun listen(): Flow<UserSettings> = dataStore.data.filterNotNull()

    override suspend fun get(): UserSettings = dataStore.data.firstOrNull()
        ?: UserSettings.Default

    override suspend fun release() {
        dataStore.updateData { UserSettings.Default }
    }
}
