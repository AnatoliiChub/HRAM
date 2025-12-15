package com.achub.hram.di

import com.achub.hram.di.ble.BleDataModule
import com.achub.hram.di.data.DataModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        ViewModelModule::class,
        BleDataModule::class,
        TrackingModule::class,
        DataModule::class,
        UtilsModule::class,
    ]
)
object AppModule
