package com.achub.hram

import android.app.Application
import com.achub.hram.di.dataModule
import com.achub.hram.di.viewModelModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.core.context.GlobalContext.startKoin

class HramApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        startKoin {
            modules(viewModelModule, dataModule)
        }
    }
}