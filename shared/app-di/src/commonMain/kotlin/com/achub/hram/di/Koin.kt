package com.achub.hram.di

import com.achub.hram.Logger
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.startKoin

fun initKoin(config: KoinAppDeclaration? = null) {
    Logger.init()
    AppModule.startKoin {
        config?.invoke(this)
    }
}

