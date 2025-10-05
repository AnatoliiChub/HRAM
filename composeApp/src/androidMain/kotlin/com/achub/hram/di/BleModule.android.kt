package com.achub.hram.di

import com.achub.hram.BluetoothState
import com.achub.hram.BluetoothStateAndroid
import com.achub.hram.HramApp
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class BleModule {
    @Single
    actual fun provideBluetoothState(): BluetoothState = BluetoothStateAndroid(HramApp.context!!)
}
