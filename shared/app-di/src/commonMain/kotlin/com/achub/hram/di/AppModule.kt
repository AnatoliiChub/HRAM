package com.achub.hram.di

import com.achub.hram.data.di.DataModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        ViewModelModule::class,
        TrackingModule::class,
        UseCaseModule::class,
        TrackingPlatformModule::class,
        DataModule::class,
        UtilsModule::class,
        ExportModule::class,
    ]
)
object AppModule
