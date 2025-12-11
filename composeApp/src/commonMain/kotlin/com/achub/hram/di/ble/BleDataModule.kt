package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.HramHrDeviceRepo
import com.achub.hram.ble.core.BleConnectionManager
import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.BleParser
import com.achub.hram.ble.core.HramBleConnectionManager
import com.achub.hram.ble.core.HramBleDataRepo
import com.achub.hram.ble.core.HramBleParser
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [BleModule::class])
@Configuration
class BleDataModule {
    @Single
    fun bleConnectionManager(
        @Provided bluetoothState: BluetoothState,
        @InjectedParam scope: CoroutineScope
    ): BleConnectionManager = HramBleConnectionManager(bluetoothState, scope)

    @Single(binds = [BleDataRepo::class])
    fun bleDataRepo(parser: BleParser): BleDataRepo = HramBleDataRepo(parser)

    @Single
    fun hrDeviceRepo(
        @InjectedParam scope: CoroutineScope,
        bleDataRepo: BleDataRepo,
        bleConnectionManager: BleConnectionManager
    ): HrDeviceRepo = HramHrDeviceRepo(scope, bleDataRepo, bleConnectionManager)

    @Single
    fun provideBleParser(): BleParser = HramBleParser()
}
