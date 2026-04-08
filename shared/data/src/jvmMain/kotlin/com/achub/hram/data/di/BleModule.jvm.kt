package com.achub.hram.data.di

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.BluetoothStateJvm
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
actual class BleModule actual constructor() {
    @Single
    actual fun provideBluetoothState(scope: Scope): BluetoothState = BluetoothStateJvm()
}
