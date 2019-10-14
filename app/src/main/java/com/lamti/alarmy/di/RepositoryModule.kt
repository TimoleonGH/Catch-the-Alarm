package com.lamti.alarmy.di

import android.app.Application
import androidx.annotation.NonNull
import androidx.room.Room
import com.lamti.alarmy.data.local.AlarmsDao
import com.lamti.alarmy.data.local.AlarmsDatabase
import com.lamti.alarmy.ui.AlarmsAdapter
import com.lamti.alarmy.ui.SimpleAlarmAdapter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { provideDatabase(get()) }
    single { provideMovieDao(get()) }
    single { (interaction:AlarmsAdapter.Interaction) -> AlarmsAdapter( interaction, get() ) }
    single { (interaction:SimpleAlarmAdapter.Interaction) -> SimpleAlarmAdapter( interaction, get() ) }

}


fun provideDatabase(@NonNull application: Application): AlarmsDatabase {
    return Room
        .databaseBuilder(application, AlarmsDatabase::class.java, "alarms.db")
        .allowMainThreadQueries()
        .build()
}

fun provideMovieDao(@NonNull database: AlarmsDatabase): AlarmsDao { return database.alarmsDao() }