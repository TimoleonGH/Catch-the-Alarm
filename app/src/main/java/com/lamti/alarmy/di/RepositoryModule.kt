package com.lamti.alarmy.di

import android.app.Application
import androidx.annotation.NonNull
import androidx.room.Room
import com.lamti.alarmy.data.local.AlarmsDao
import com.lamti.alarmy.data.local.AlarmsDatabase
import com.lamti.alarmy.ui.main_activity.AlarmAdapter
import org.koin.dsl.module

val repositoryModule = module {
    single { provideDatabase(get()) }
    single { provideMovieDao(get()) }
    single { (interaction: AlarmAdapter.Interaction) -> AlarmAdapter( interaction, get() ) }
}


fun provideDatabase(@NonNull application: Application): AlarmsDatabase {
    return Room
        .databaseBuilder(application, AlarmsDatabase::class.java, "alarms.db")
        .allowMainThreadQueries()
        .build()
}

fun provideMovieDao(@NonNull database: AlarmsDatabase): AlarmsDao { return database.alarmsDao() }
