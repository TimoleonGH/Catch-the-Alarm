package com.lamti.alarmy.domain.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lamti.alarmy.data.models.Alarm
import java.util.*
import kotlin.collections.ArrayList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.domain.receivers.AlarmyReceiver
import com.lamti.alarmy.domain.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.domain.utils.getTimeInMillis


object AlarmyManager {

    private var alarmMgr: AlarmManager? = null

    fun addAlarm(alarm: Alarm, context: Context, nextAlarmFlag: Boolean = false) {
        initAlarmyManager(context)
        val broadcastIntent = initBroadcastIntent(context, alarm)
        val pendingIntent = initPendingIntent(context, alarm, broadcastIntent)
        createAlarm(alarm, pendingIntent, nextAlarmFlag)
    }

    private fun initAlarmyManager(context: Context) {
        if (alarmMgr == null)
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun initBroadcastIntent(context: Context, alarm: Alarm): Intent {
        val broadcastIntent = Intent(context, AlarmyReceiver::class.java)
        val type = object : TypeToken<Alarm>() {}.type
        val json = Gson().toJson(alarm, type)
        broadcastIntent.putExtra(ALARM_DATA_EXTRA, json)

        return broadcastIntent
    }

    private fun initPendingIntent(
        context: Context,
        alarm: Alarm,
        broadcastIntent: Intent
    ): PendingIntent {
        return PendingIntent.getBroadcast(
            context.applicationContext,
            alarm.id,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createAlarm(alarm: Alarm, pendingIntent: PendingIntent, nextAlarmFlag: Boolean) {
        val hasAlarmRepeatingDays = alarm.intDays.isNullOrEmpty()
        if (hasAlarmRepeatingDays) {
            addOneTimeAlarm(alarm, pendingIntent)
        } else
            addRepeatingAlarm(alarm, pendingIntent, nextAlarmFlag)
    }

    private fun addOneTimeAlarm(alarm: Alarm, pendingIntent: PendingIntent) {
        val isAlarmTimeGreaterThanCurrentTime =
            System.currentTimeMillis() < alarm.time.getTimeInMillis()
        val info = if (isAlarmTimeGreaterThanCurrentTime)
            AlarmManager.AlarmClockInfo(alarm.time.getTimeInMillis(), pendingIntent)
        else
            addOneTimeAlarmForNextDay(alarm, pendingIntent)

        alarmMgr?.setAlarmClock(info, pendingIntent)
    }

    private fun addOneTimeAlarmForNextDay(
        alarm: Alarm,
        pendingIntent: PendingIntent
    ): AlarmManager.AlarmClockInfo {
        return AlarmManager.AlarmClockInfo(
            alarm.time.getTimeInMillis() + AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun addRepeatingAlarm(alarm: Alarm, pIntent: PendingIntent, nextAlarmFlag: Boolean) {
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
        initAlarmyManager(context)
        val pendingIntent = initCancelAlarmPendingIntent(context, alarm)
        alarmMgr?.cancel(pendingIntent)
    }

    private fun initCancelAlarmPendingIntent(context: Context, alarm: Alarm): PendingIntent {
        val broadcastIntent = Intent(context, AlarmyReceiver::class.java)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            alarm.id,
            broadcastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun cancelAllAlarms(alarms: ArrayList<Alarm>, context: Context) {
        alarms.forEach { alarm ->
            val pendingIntent = initCancelAlarmPendingIntent(context, alarm)
            alarmMgr?.cancel(pendingIntent)
        }
    }
}
