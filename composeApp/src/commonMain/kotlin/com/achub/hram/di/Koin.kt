package com.achub.hram.di

import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.startKoin

fun initKoin(config: KoinAppDeclaration? = null) {
    AppModule.startKoin {
        config?.invoke(this)
    }
}
