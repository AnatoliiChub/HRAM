package com.achub.hram

import com.achub.hram.di.dataModule
import com.achub.hram.di.viewModelModule
import org.koin.core.context.startKoin

fun init(){
    startKoin {
        modules(viewModelModule, dataModule)
    }
}
