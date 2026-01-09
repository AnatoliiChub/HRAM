package com.achub.hram.di.data

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
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Module(includes = [CoroutineModule::class, JsonModule::class])
@Configuration
actual class DataStoreModule {
    @OptIn(ExperimentalForeignApi::class)
    @Single
    @Named(BLE_STATE_QUALIFIER)
    actual fun provideBleStateDataStore(
        scope: Scope,
        serializer: BleStateSerializer
    ): DataStore<BleState> = createOkioDataStore(
        produceFilePath = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$BLE_STATE_FILE_NAME"
        },
        serializer = serializer
    )

    @OptIn(ExperimentalForeignApi::class)
    @Single
    @Named(TRACKING_STATE_QUALIFIER)
    actual fun provideTrackingStateStageDataStore(
        scope: Scope,
        serializer: TrackingStateStageSerializer
    ): DataStore<TrackingStateStage> = createOkioDataStore(
        produceFilePath = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$TRACKING_STATE_STAGE_FILE_NAME"
        },
        serializer = serializer
    )
}
