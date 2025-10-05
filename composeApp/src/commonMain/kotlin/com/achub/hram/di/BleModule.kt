package com.achub.hram.di

import com.achub.hram.BluetoothState
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
expect class BleModule() {

    @Single
    fun provideBluetoothState() : BluetoothState
}
