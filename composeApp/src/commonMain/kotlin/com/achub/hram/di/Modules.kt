package com.achub.hram.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [ViewModelModule::class, DataModule::class])
@ComponentScan("com.achub.hram")
class AppModule
