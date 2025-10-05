package com.achub.hram

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.achub.hram.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class HramApp : Application() {

    companion object {
        //Application context existing all the time while app is running
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
            private set
    }
    override fun onCreate() {
        super.onCreate()
        Napier.base(DebugAntilog())
        initKoin {
            //TODO use this context instead of static in the app
            androidContext(this@HramApp)
        }
        context = this
    }
}
