package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.core.scope.Scope

@Module
@Configuration
expect class BleModule() {
    @Single
    fun provideBluetoothState(scope: Scope): BluetoothState
}
