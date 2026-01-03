package com.achub.hram.di.data

import androidx.datastore.core.DataStore
import com.achub.hram.data.TrackingStateSerializer
import com.achub.hram.data.models.BleState
import com.achub.hram.di.CoroutineModule
import com.achub.hram.di.JsonModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module(includes = [CoroutineModule::class, JsonModule::class])
@Configuration
expect class DataStoreModule() {
    @Single
    fun provideTrackingStateDataStore(scope: Scope, serializer: TrackingStateSerializer): DataStore<BleState>
}
