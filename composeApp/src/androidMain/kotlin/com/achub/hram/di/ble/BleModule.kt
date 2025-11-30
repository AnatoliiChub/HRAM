package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.BluetoothStateAndroid
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
actual class BleModule {
    @Single
    actual fun provideBluetoothState(scope: Scope): BluetoothState = BluetoothStateAndroid(scope.get())
}
