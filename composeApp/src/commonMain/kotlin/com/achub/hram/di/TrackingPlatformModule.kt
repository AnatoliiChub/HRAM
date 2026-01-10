package com.achub.hram.di

import com.achub.hram.tracking.TrackingController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module()
@Configuration
expect class TrackingPlatformModule() {
    @Single
    fun provideTrackingController(scope: Scope): TrackingController
}
