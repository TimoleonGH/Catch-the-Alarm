package com.lamti.alarmy.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "alarm_id")
    var id: Int = 0,
    @ColumnInfo(name = "alarm_time")
    var time: String,
    @ColumnInfo(name = "alarm_mili_time")
    var miliTime: Long,
    @ColumnInfo(name = "alarm_message")
    var message: String,
    @ColumnInfo(name = "alarm_days")
    var days: List<String>?,
    @ColumnInfo(name = "alarm_int_days")
    var intDays: List<Int>?,
    @ColumnInfo(name = "alarm_game")
    var game: Boolean,
    @ColumnInfo(name = "alarm_snooze")
    var snooze: Boolean,
    @ColumnInfo(name = "alarm_vibration")
    var vibration: Boolean,
    @ColumnInfo(name = "alarm_is_on")
    var isOn: Boolean )
