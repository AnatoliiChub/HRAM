package com.achub.hram.di

import com.achub.hram.utils.ActivityNameValidation
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
class UtilsModule {
    @Factory
    fun provideActivityNameValidationUseCase() = ActivityNameValidation()
}
