package com.achub.hram.di

import com.achub.hram.data.TrackingStateSerializer
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
    fun provideTrackingStateSerializer(json: Json): TrackingStateSerializer = TrackingStateSerializer(json)
}
