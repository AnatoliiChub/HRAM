package com.achub.hram.di

import com.achub.hram.data.BleConnectionRepo
import com.achub.hram.data.BleHrDataRepo
import com.achub.hram.data.HramBleConnectionRepo
import com.achub.hram.data.HramBleHrDataRepo
import org.koin.dsl.module

val dataModule = module {
    single<BleConnectionRepo> { HramBleConnectionRepo() }
    single<BleHrDataRepo> { HramBleHrDataRepo() }
}