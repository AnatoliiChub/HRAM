package com.achub.hram

import android.app.Application
import com.achub.hram.di.NotificationModule
import com.achub.hram.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.ksp.generated.module

class HramApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HramApp)
            modules(NotificationModule().module)
        }
    }
}
