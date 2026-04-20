package com.achub.hram.data.di

import com.achub.hram.data.store.BleStateSerializer
import com.achub.hram.data.store.TrackingStateStageSerializer
import com.achub.hram.data.store.UserSettingsSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class SerializerModule {
    @Single
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Single
    fun provideBleStateSerializer(json: Json): BleStateSerializer = BleStateSerializer(json)

    @Single
    fun provideTrackingStateStageSerializer(json: Json): TrackingStateStageSerializer =
        TrackingStateStageSerializer(json)

    @Single
    fun provideUserSettingsSerializer(json: Json): UserSettingsSerializer =
        UserSettingsSerializer(json)
}

