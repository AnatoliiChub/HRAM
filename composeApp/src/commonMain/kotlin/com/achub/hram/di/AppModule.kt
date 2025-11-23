package com.achub.hram.di

import com.achub.hram.di.ble.BleDataModule
import com.achub.hram.di.data.DataModule
import org.koin.core.annotation.Module

@Module(
    includes = [ViewModelModule::class,
        BleDataModule::class,
        TrackingModule::class,
        DataModule::class,
        UtilsModule::class]
)
class AppModule
