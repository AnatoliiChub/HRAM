package com.achub.hram.di

import com.achub.hram.ActivityNameErrorMapper
import com.achub.hram.tracking.BlePlatformStateHandler
import com.achub.hram.tracking.MokoBlePlatformStateHandler
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
class UtilsModule {
    @Single(binds = [BlePlatformStateHandler::class])
    fun provideBlePlatformStateHandler(): BlePlatformStateHandler = MokoBlePlatformStateHandler()

    @Factory
    fun provideActivityNameValidationUseCase() = ActivityNameErrorMapper()
}
