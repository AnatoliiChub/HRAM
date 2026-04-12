package com.achub.hram.di

import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.tracking.BlePlatformStateHandler
import com.achub.hram.tracking.NoOpBlePlatformStateHandler
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
actual class UtilsModule actual constructor() {
    @Single(binds = [BlePlatformStateHandler::class])
    actual fun provideBlePlatformStateHandler(): BlePlatformStateHandler = NoOpBlePlatformStateHandler()

    @Factory
    actual fun provideActivityNameValidationUseCase(): ActivityNameErrorMapper = ActivityNameErrorMapper()
}

