package com.achub.hram.data.di

import androidx.datastore.core.DataStore
import com.achub.hram.data.store.BleStateSerializer
import com.achub.hram.data.store.TrackingStateStageSerializer
import com.achub.hram.data.store.UserSettingsSerializer
import com.achub.hram.di.CoroutineModule
import com.achub.hram.models.BleState
import com.achub.hram.models.UserSettings
import com.achub.hram.tracking.TrackingStateStage
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

const val BLE_STATE_QUALIFIER = "BleState"
const val TRACKING_STATE_QUALIFIER = "TrackingStateStage"
const val USER_SETTINGS_QUALIFIER = "UserSettings"

@Module(includes = [CoroutineModule::class, SerializerModule::class])
@Configuration
expect class DataStoreModule() {
    @Single
    @Named(BLE_STATE_QUALIFIER)
    fun provideBleStateDataStore(scope: Scope, serializer: BleStateSerializer): DataStore<BleState>

    @Single
    @Named(TRACKING_STATE_QUALIFIER)
    fun provideTrackingStateStageDataStore(
        scope: Scope,
        serializer: TrackingStateStageSerializer
    ): DataStore<TrackingStateStage>

    @Single
    @Named(USER_SETTINGS_QUALIFIER)
    fun provideUserSettingsDataStore(scope: Scope, serializer: UserSettingsSerializer): DataStore<UserSettings>
}
