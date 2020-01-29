package com.lamti.alarmy

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import com.lamti.alarmy.di.appModules
import com.lamti.alarmy.domain.receivers.RebootReceiver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Alarmy : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@Alarmy)
            modules(appModules)
        }

        enableAlarm()
    }

    private fun enableAlarm() {
        val receiver = ComponentName(applicationContext, RebootReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
