package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.HrDeviceRepo
import com.achub.hram.ble.HramHrDeviceRepo
import com.achub.hram.ble.core.BleDataRepo
import com.achub.hram.ble.core.BleParser
import com.achub.hram.ble.core.HramBleDataRepo
import com.achub.hram.ble.core.HramBleParser
import com.achub.hram.ble.core.connection.BleConnector
import com.achub.hram.ble.core.connection.BleScanner
import com.achub.hram.ble.core.connection.ConnectionTracker
import com.achub.hram.ble.core.connection.HramBleConnectionManager
import com.achub.hram.ble.core.connection.HramBleConnector
import com.achub.hram.ble.core.connection.HramBleScanner
import com.achub.hram.ble.core.connection.HramConnectionTracker
import com.achub.hram.ble.models.HramPeripheralConvertor
import com.achub.hram.ble.models.PeripheralConvertor
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
    fun bleScanner(): BleScanner = HramBleScanner()

    @Single
    fun bleConnector(): BleConnector = HramBleConnector()

    @Single(binds = [BleDataRepo::class])
    fun bleDataRepo(parser: BleParser): BleDataRepo = HramBleDataRepo(parser)

    @Single
    fun hrDeviceRepo(
        @InjectedParam scope: CoroutineScope,
        connectionTracker: ConnectionTracker,
        bleDataRepo: BleDataRepo,
        @WorkerThread dispatcher: CoroutineDispatcher,
        bleScanner: BleScanner,
        bleConnector: BleConnector,
        peripheralConvertor: PeripheralConvertor,
    ): HrDeviceRepo = HramHrDeviceRepo(
        scope,
        bleDataRepo,
        dispatcher,
        // Assisted injection in AssistedInject is not supported
        HramBleConnectionManager(connectionTracker, bleScanner, bleConnector, peripheralConvertor, scope)
    )

    @Single
    fun provideBleParser(): BleParser = HramBleParser()

    @Single
    fun provideConnectionTracker(@Provided bluetoothState: BluetoothState): ConnectionTracker =
        HramConnectionTracker(bluetoothState)

    @Single
    fun providePeripheralConvertor(): PeripheralConvertor = HramPeripheralConvertor()
}
