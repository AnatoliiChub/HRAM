package com.achub.hram.di

import com.achub.hram.BluetoothState
import com.achub.hram.data.BleConnectionRepo
import com.achub.hram.data.BleDataRepo
import com.achub.hram.data.HramBleConnectionRepo
import com.achub.hram.data.HramBleDataRepo
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module(includes = [BleModule::class])
class DataModule() {
    @Single
    fun provideBleConnectionRepo(@Provided bluetoothState: BluetoothState): BleConnectionRepo =
        HramBleConnectionRepo(bluetoothState)

    @Single
    fun provideBleHrDataRepo() : BleDataRepo = HramBleDataRepo()

}
