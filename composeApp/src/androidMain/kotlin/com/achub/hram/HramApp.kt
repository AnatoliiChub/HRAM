package com.achub.hram

import android.app.Application
import com.achub.hram.di.viewModelModule
import org.koin.core.context.GlobalContext.startKoin

class HramApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(viewModelModule)
        }
    }
}