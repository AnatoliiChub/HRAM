package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.HramHrDeviceRepo
import com.achub.hram.ble.core.connection.BleConnectionManager
import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.BleParser
import com.achub.hram.ble.core.connection.ConnectionTracker
import com.achub.hram.ble.core.connection.HramBleConnectionManager
import com.achub.hram.ble.core.HramBleDataRepo
import com.achub.hram.ble.core.HramBleParser
import com.achub.hram.ble.core.connection.HramConnectionTracker
import com.achub.hram.di.CoroutineModule
import com.achub.hram.di.WorkerThread
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [BleModule::class, CoroutineModule::class])
@Configuration
class BleDataModule {
    @Single
    fun bleConnectionManager(
        connectionTracker: ConnectionTracker,
        @InjectedParam scope: CoroutineScope
    ): BleConnectionManager = HramBleConnectionManager(connectionTracker, scope)

    @Single(binds = [BleDataRepo::class])
    fun bleDataRepo(parser: BleParser): BleDataRepo = HramBleDataRepo(parser)

    @Single
    fun hrDeviceRepo(
        @InjectedParam scope: CoroutineScope,
        bleDataRepo: BleDataRepo,
        bleConnectionManager: BleConnectionManager,
        @WorkerThread dispatcher: CoroutineDispatcher,
    ): HrDeviceRepo = HramHrDeviceRepo(scope, bleDataRepo, bleConnectionManager, dispatcher)

    @Single
    fun provideBleParser(): BleParser = HramBleParser()

    @Single
    fun provideConnectionTracker(@Provided bluetoothState: BluetoothState): ConnectionTracker =
        HramConnectionTracker(bluetoothState)
}
