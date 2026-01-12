package com.achub.hram.di

import com.achub.hram.data.store.BleStateSerializer
import com.achub.hram.data.store.TrackingStateStageSerializer
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class JsonModule {
    @Single
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Single
    fun provideBleStateSerializer(json: Json): BleStateSerializer = BleStateSerializer(json)

    @Single
    fun provideTrackingStateStageSerializer(json: Json): TrackingStateStageSerializer =
        TrackingStateStageSerializer(json)
}
