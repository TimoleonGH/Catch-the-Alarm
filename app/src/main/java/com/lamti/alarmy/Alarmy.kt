package com.lamti.alarmy

import android.app.Application
import com.lamti.alarmy.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Alarmy : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Alarmy)
            modules(appModules)
        }
    }
}