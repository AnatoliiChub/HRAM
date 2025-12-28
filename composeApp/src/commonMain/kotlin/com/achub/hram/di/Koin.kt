package com.achub.hram.di

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.dsl.KoinAppDeclaration
import org.koin.ksp.generated.startKoin

fun initKoin(config: KoinAppDeclaration? = null) {
    Napier.base(DebugAntilog())
    AppModule.startKoin {
        config?.invoke(this)
    }
}
