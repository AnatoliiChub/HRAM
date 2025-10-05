package com.achub.hram.di

import com.achub.hram.BluetoothState
import com.achub.hram.BluetoothStateIos
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class BleModule actual constructor() {
    @Single
    actual fun provideBluetoothState(): BluetoothState = BluetoothStateIos()
}
