package com.achub.hram.domain

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DomainModule {

    @Single
    fun provideActivityNameValidationUseCase() = ActivityNameValidationUseCase()
}
