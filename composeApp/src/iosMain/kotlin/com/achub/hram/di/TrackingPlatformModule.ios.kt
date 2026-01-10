package com.achub.hram.di

import com.achub.hram.tracking.TrackingController
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
actual class TrackingPlatformModule actual constructor() {
    @Single
    actual fun provideTrackingController(scope: Scope): TrackingController {
        return TrackingController()
    }
}
