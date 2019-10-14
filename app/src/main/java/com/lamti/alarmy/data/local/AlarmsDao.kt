package com.lamti.alarmy.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lamti.alarmy.data.models.Alarm

@Dao
interface AlarmsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(t: Alarm): Long

    @Query(value = "SELECT * FROM alarms")
    fun alarms(): LiveData<List<Alarm>>

    @Update
    suspend fun updateAlarm(t: Alarm)

    @Delete
    suspend fun deleteAlarm(t: Alarm)

    @Query(value = "DELETE FROM alarms")
    suspend fun deleteAll()
}