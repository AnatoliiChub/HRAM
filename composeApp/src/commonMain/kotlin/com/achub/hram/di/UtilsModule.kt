package com.achub.hram.di

import com.achub.hram.domain.ActivityNameValidator
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
class UtilsModule {

    @Factory
    fun provideActivityNameValidationUseCase() = ActivityNameValidator()
}