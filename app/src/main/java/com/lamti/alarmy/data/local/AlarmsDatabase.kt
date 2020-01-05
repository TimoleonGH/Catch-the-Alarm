package com.lamti.alarmy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lamti.alarmy.data.models.Alarm

@Database(entities = [Alarm::class], version = 1, exportSchema = false)
@TypeConverters(value = [(StringListConverter::class), (IntListConverter::class)])
abstract class AlarmsDatabase: RoomDatabase() {
    abstract fun alarmsDao(): AlarmsDao
}