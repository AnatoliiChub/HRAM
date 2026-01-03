package com.achub.hram.di.data

import android.content.Context
import androidx.datastore.core.DataStore
import com.achub.hram.data.TrackingStateSerializer
import com.achub.hram.data.models.BleState
import com.achub.hram.data.store.DATA_STORE_FILE_NAME
import com.achub.hram.data.store.createTrackingStateDataStore
import com.achub.hram.di.CoroutineModule
import com.achub.hram.di.JsonModule
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module(includes = [CoroutineModule::class, JsonModule::class])
@Configuration
actual class DataStoreModule actual constructor() {
    @Single
    actual fun provideTrackingStateDataStore(
        scope: Scope,
        serializer: TrackingStateSerializer
    ): DataStore<BleState> = createTrackingStateDataStore(
        produceFilePath = { scope.get<Context>().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath },
        serializer = serializer
    )
}
