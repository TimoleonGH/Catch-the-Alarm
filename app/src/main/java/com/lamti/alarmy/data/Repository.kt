package com.lamti.alarmy.data

import androidx.lifecycle.LiveData
import com.lamti.alarmy.data.local.AlarmsDao
import com.lamti.alarmy.data.models.Alarm

class Repository ( private val alarmsDao: AlarmsDao) {

    val allAlarms: LiveData<List<Alarm>> = alarmsDao.alarms()

    suspend fun insert(alarm: Alarm) {
        alarmsDao.insertAlarm(alarm)
    }

    suspend fun update(alarm: Alarm) {
        alarmsDao.updateAlarm(alarm)
    }

    suspend fun deleteAll() {
        alarmsDao.deleteAll()
    }
}