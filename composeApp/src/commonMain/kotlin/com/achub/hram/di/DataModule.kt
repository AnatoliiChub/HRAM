package com.achub.hram.di

import com.achub.hram.data.BleRepo
import com.achub.hram.data.HramBleRepo
import org.koin.dsl.module

val dataModule = module{
    single<BleRepo> { HramBleRepo() }
}