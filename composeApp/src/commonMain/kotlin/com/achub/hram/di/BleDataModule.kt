package com.achub.hram.di

import com.achub.hram.ble.BleFactory
import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.core.connection.BleConnector
import com.achub.hram.ble.core.connection.BleScanner
import com.achub.hram.ble.core.connection.ConnectionTracker
import com.achub.hram.ble.core.data.BleDataRepo
import com.achub.hram.ble.core.data.BleParser
import com.achub.hram.ble.models.PeripheralConvertor
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [BleModule::class])
@Configuration
class BleDataModule {
    @Factory
    fun bleScanner(): BleScanner = BleFactory.scanner()

    @Single
    fun bleConnector(): BleConnector = BleFactory.connector()

    @Single(binds = [BleDataRepo::class])
    fun bleDataRepo(parser: BleParser): BleDataRepo = BleFactory.dataRepo(parser)

    @Single
    fun hrDeviceRepo(
        @InjectedParam scope: CoroutineScope,
        connectionTracker: ConnectionTracker,
        bleDataRepo: BleDataRepo,
        bleScanner: BleScanner,
        bleConnector: BleConnector,
        peripheralConvertor: PeripheralConvertor,
    ): HrDeviceRepo =
        BleFactory.hrDeviceRepo(scope, connectionTracker, bleDataRepo, bleScanner, bleConnector, peripheralConvertor)

    @Single
    fun provideBleParser(): BleParser = BleFactory.parser()

    @Single
    fun provideConnectionTracker(@Provided bluetoothState: BluetoothState): ConnectionTracker =
        BleFactory.connectionTracker(bluetoothState)

    @Single
    fun providePeripheralConvertor(): PeripheralConvertor = BleFactory.peripheralConvertor()
}
