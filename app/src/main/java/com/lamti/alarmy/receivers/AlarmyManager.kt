package com.lamti.alarmy.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lamti.alarmy.data.models.Alarm
import java.util.*
import kotlin.collections.ArrayList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.getTimeInMillis


object AlarmyManager {

    private var alarmMgr: AlarmManager? = null

    private fun initAlarmyManager(context: Context) {
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun addAlarm(alarm: Alarm, context: Context, nextAlarmFlag: Boolean = false) {
        if (alarmMgr == null)
            initAlarmyManager(context)

        // Intent to start the Broadcast Receiver
        val broadcastIntent = Intent(context, AlarmyReceiver::class.java)
        val type = object : TypeToken<Alarm>() {}.type
        val json = Gson().toJson(alarm, type)
        broadcastIntent.putExtra(ALARM_DATA_EXTRA, json)
        val pIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            alarm.id,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // one time alarm
        if (alarm.intDays.isNullOrEmpty()) {
            val info = if (System.currentTimeMillis() < alarm.time.getTimeInMillis())
                AlarmManager.AlarmClockInfo(alarm.time.getTimeInMillis(), pIntent)
            else
                AlarmManager.AlarmClockInfo(
                    alarm.time.getTimeInMillis() + AlarmManager.INTERVAL_DAY,
                    pIntent
                )

            alarmMgr?.setAlarmClock(info, pIntent)
        } else
            addRepeatingAlarm(alarm, context, pIntent, nextAlarmFlag)
    }

    private fun addRepeatingAlarm(alarm: Alarm, context: Context, pIntent: PendingIntent, nextAlarmFlag: Boolean) {
        var currentWeekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var alarmDay = currentWeekDay
        var alarmDayFound = false

        // if alarm time is sooner than current time (or alarm stopped from AlarmyActivity) go to next day
        if (System.currentTimeMillis() > alarm.miliTime || nextAlarmFlag) {
            currentWeekDay += 1
            alarmDay = currentWeekDay
        }

        // find the alarm day of the week starting from current day of week (1 = "Sunday")
        alarm.intDays!!.forEach {
            if (currentWeekDay <= it && !alarmDayFound) {
                alarmDay = it
                alarmDayFound = true
            }
        }

        // if no day found go to first day
        if (!alarmDayFound)
            alarmDay = alarm.intDays!![0]

        // find the alarm day of the month (Sunday 4 October)
        val alarmCalendar: Calendar = Calendar.getInstance()
        while (alarmCalendar.get(Calendar.DAY_OF_WEEK) != alarmDay) {
            alarmCalendar.add(Calendar.DATE, 1)
        }

        // set the alarm time
        val aca = Calendar.getInstance()
        aca.timeInMillis = alarm.miliTime
        alarmCalendar.set(Calendar.HOUR_OF_DAY, aca.get(Calendar.HOUR_OF_DAY))
        alarmCalendar.set(Calendar.MINUTE, aca.get(Calendar.MINUTE))
        alarmCalendar.set(Calendar.SECOND, 0)

        // set the alarm to alarm date (day and time)
        val info = AlarmManager.AlarmClockInfo(alarmCalendar.timeInMillis, pIntent)
        alarmMgr?.setAlarmClock(info, pIntent)
    }

    fun updateAlarm(alarm: Alarm, context: Context) {
        if (alarm.isOn)
            addAlarm(alarm, context)
        else
            cancelAlarm(alarm, context)
    }

    fun cancelAlarm(alarm: Alarm, context: Context) {
        if (alarmMgr == null)
            initAlarmyManager(context)

        // Intent to start the Broadcast Receiver
        val broadcastIntent = Intent(context, AlarmyReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            alarm.id,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmMgr?.cancel(pIntent)
    }

    fun cancelAllAlarms(alarms: ArrayList<Alarm>, context: Context) {
        // Intent to start the Broadcast Receiver
        val broadcastIntent = Intent(context, AlarmyReceiver::class.java)

        alarms.forEach { alarm ->
            val pIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                alarm.id,
                broadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmMgr?.cancel(pIntent)
        }
    }

}
