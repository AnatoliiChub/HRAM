package com.achub.hram.di.ble

import com.achub.hram.ble.BluetoothState
import com.achub.hram.ble.repo.BleConnectionRepo
import com.achub.hram.ble.repo.BleDataRepo
import com.achub.hram.ble.repo.HrDeviceRepo
import com.achub.hram.ble.repo.HramBleConnectionRepo
import com.achub.hram.ble.repo.HramBleDataRepo
import com.achub.hram.ble.repo.HramHrDeviceRepo
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [BleModule::class])
class BleDataModule {

    @Single
    fun bleConnectionRepo(
        bluetoothState: BluetoothState,
        @InjectedParam scope: CoroutineScope
    ): BleConnectionRepo = HramBleConnectionRepo(bluetoothState, scope)

    @Single(binds = [BleDataRepo::class])
    fun bleDataRepo(): BleDataRepo = HramBleDataRepo()

    @Single
    fun hrDeviceRepo(
        @InjectedParam scope: CoroutineScope,
        bleDataRepo: BleDataRepo,
        bleConnectionRepo: BleConnectionRepo
    ): HrDeviceRepo = HramHrDeviceRepo(scope, bleDataRepo, bleConnectionRepo)

}