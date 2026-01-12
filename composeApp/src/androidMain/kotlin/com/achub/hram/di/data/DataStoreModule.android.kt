package com.achub.hram.di.data

import android.content.Context
import androidx.datastore.core.DataStore
import com.achub.hram.data.models.BleState
import com.achub.hram.data.store.BLE_STATE_FILE_NAME
import com.achub.hram.data.store.BleStateSerializer
import com.achub.hram.data.store.TRACKING_STATE_STAGE_FILE_NAME
import com.achub.hram.data.store.TrackingStateStageSerializer
import com.achub.hram.data.store.createOkioDataStore
import com.achub.hram.di.CoroutineModule
import com.achub.hram.di.JsonModule
import com.achub.hram.tracking.TrackingStateStage
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module(includes = [CoroutineModule::class, JsonModule::class])
@Configuration
actual class DataStoreModule {
    @Single
    @Named(BLE_STATE_QUALIFIER)
    actual fun provideBleStateDataStore(
        scope: Scope,
        serializer: BleStateSerializer
    ): DataStore<BleState> = createOkioDataStore(
        produceFilePath = { scope.get<Context>().filesDir.resolve(BLE_STATE_FILE_NAME).absolutePath },
        serializer = serializer
    )

    @Single
    @Named(TRACKING_STATE_QUALIFIER)
    actual fun provideTrackingStateStageDataStore(
        scope: Scope,
        serializer: TrackingStateStageSerializer
    ): DataStore<TrackingStateStage> = createOkioDataStore(
        produceFilePath = { scope.get<Context>().filesDir.resolve(TRACKING_STATE_STAGE_FILE_NAME).absolutePath },
        serializer = serializer
    )
}
