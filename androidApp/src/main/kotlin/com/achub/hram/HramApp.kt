package com.achub.hram

import android.app.Application
import com.achub.hram.di.initKoin
import org.koin.android.ext.koin.androidContext

class HramApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@HramApp)
        }
    }
}
