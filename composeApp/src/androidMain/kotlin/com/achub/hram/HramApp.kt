package com.achub.hram

import android.app.Application
import com.achub.hram.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.ksp.generated.defaultModule


class HramApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        initKoin {
            androidContext(this@HramApp)
                .defaultModule()
        }
    }
}
