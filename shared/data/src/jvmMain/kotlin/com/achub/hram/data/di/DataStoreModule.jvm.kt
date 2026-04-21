package com.achub.hram.data.di

import androidx.datastore.core.DataStore
import com.achub.hram.data.store.BLE_STATE_FILE_NAME
import com.achub.hram.data.store.BleStateSerializer
import com.achub.hram.data.store.TRACKING_STATE_STAGE_FILE_NAME
import com.achub.hram.data.store.TrackingStateStageSerializer
import com.achub.hram.data.store.USER_SETTINGS_FILE_NAME
import com.achub.hram.data.store.UserSettingsSerializer
import com.achub.hram.data.store.createOkioDataStore
import com.achub.hram.di.CoroutineModule
import com.achub.hram.models.BleState
import com.achub.hram.models.UserSettings
import com.achub.hram.tracking.TrackingStateStage
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope
import java.io.File

@Module(includes = [CoroutineModule::class, SerializerModule::class])
@Configuration
actual class DataStoreModule actual constructor() {
    @Single
    @Named(BLE_STATE_QUALIFIER)
    actual fun provideBleStateDataStore(
        scope: Scope,
        serializer: BleStateSerializer,
    ): DataStore<BleState> = createOkioDataStore(
        produceFilePath = { dataStorePath(BLE_STATE_FILE_NAME) },
        serializer = serializer,
    )

    @Single
    @Named(TRACKING_STATE_QUALIFIER)
    actual fun provideTrackingStateStageDataStore(
        scope: Scope,
        serializer: TrackingStateStageSerializer,
    ): DataStore<TrackingStateStage> = createOkioDataStore(
        produceFilePath = { dataStorePath(TRACKING_STATE_STAGE_FILE_NAME) },
        serializer = serializer,
    )

    @Single
    @Named(USER_SETTINGS_QUALIFIER)
    actual fun provideUserSettingsDataStore(
        scope: Scope,
        serializer: UserSettingsSerializer,
    ): DataStore<UserSettings> = createOkioDataStore(
        produceFilePath = { dataStorePath(USER_SETTINGS_FILE_NAME) },
        serializer = serializer,
    )

    private fun dataStorePath(fileName: String): String {
        val dir = File(System.getProperty("user.home"), ".hram")
        dir.mkdirs()
        return File(dir, fileName).absolutePath
    }
}
