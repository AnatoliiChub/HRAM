package com.achub.hram.di

import com.achub.hram.ble.BleModule
import com.achub.hram.data.DataModule
import com.achub.hram.screen.ViewModelModule
import com.achub.hram.tracking.TrackingModule
import org.koin.core.annotation.Module

@Module(includes = [ViewModelModule::class, BleModule::class, TrackingModule::class, DataModule::class])
class AppModule
