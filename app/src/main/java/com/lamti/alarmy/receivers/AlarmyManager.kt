package com.lamti.alarmy.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.lamti.alarmy.data.models.Alarm
import java.util.*
import kotlin.collections.ArrayList
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.getTimeInMillis
import com.lamti.alarmy.utils.showToast


object AlarmyManager {

    private var alarmMgr: AlarmManager? = null

    private fun initAlarmyManager(context: Context) {
        // Setting up AlarmManager
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

        if (alarm.intDays.isNullOrEmpty()) {
            val info = if (System.currentTimeMillis() < alarm.time.getTimeInMillis())
                AlarmManager.AlarmClockInfo(alarm.time.getTimeInMillis(), pIntent)
            else
                AlarmManager.AlarmClockInfo(
                    alarm.time.getTimeInMillis() + AlarmManager.INTERVAL_DAY,
                    pIntent
                )

            alarmMgr?.setAlarmClock(info, pIntent)
            showNextAlarmTimeLeft(context)
        } else
            addRepeatingAlarm(alarm, context, pIntent, nextAlarmFlag)
    }

    private fun addRepeatingAlarm(alarm: Alarm, context: Context, pIntent: PendingIntent, nextAlarmFlag: Boolean) {

        var currentWeekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        var alarmDay = currentWeekDay
        var alarmDayFound = false

        Log.d("ANALARA", "Today: $currentWeekDay")

        if ( System.currentTimeMillis() > alarm.miliTime  || nextAlarmFlag) {
            Log.d("ANALARA", "Alarm time is smaller than current time")
            val cal = Calendar.getInstance()
            Log.d("ANALARA", "current time: ${cal.get(Calendar.HOUR)}:${cal.get(Calendar.MINUTE)}")
            cal.timeInMillis = alarm.miliTime
            Log.d("ANALARA", "alarm time: ${cal.get(Calendar.HOUR)}:${cal.get(Calendar.MINUTE)}")
            currentWeekDay += 1
            alarmDay = currentWeekDay
        }

        alarm.intDays!!.forEach {
            if (currentWeekDay <= it && !alarmDayFound) {
                alarmDay = it
                alarmDayFound = true
                Log.d("ANALARA", "alarm day found: $alarmDay")
            }
        }

        if (!alarmDayFound)
            alarmDay = alarm.intDays!![0]

        Log.d("ANALARA", "alarm day: $alarmDay")

        val alarmCalendar: Calendar = Calendar.getInstance()
        while (alarmCalendar.get(Calendar.DAY_OF_WEEK) != alarmDay) {
            alarmCalendar.add(Calendar.DATE, 1)
        }

        val aca = Calendar.getInstance()
        aca.timeInMillis = alarm.miliTime

        alarmCalendar.set(Calendar.HOUR, aca.get(Calendar.HOUR))
        alarmCalendar.set(Calendar.MINUTE, aca.get(Calendar.MINUTE))
        alarmCalendar.set(Calendar.SECOND, 0)

        Log.d("ANALARA", "Ring alarm at: ${alarmCalendar.time}")

        val info = AlarmManager.AlarmClockInfo(alarmCalendar.timeInMillis, pIntent)
        alarmMgr?.setAlarmClock(info, pIntent)
        showNextAlarmTimeLeft(context)
    }

    private fun showNextAlarmTimeLeft(context: Context) {

        val nextAlarmTriggerTime = alarmMgr?.nextAlarmClock?.triggerTime ?: 0
//        val nextAlarmTimeInMillis = nextAlarmTriggerTime - System.currentTimeMillis()
//        val nextAlarmTimeInMinutes = TimeUnit.MILLISECONDS.toMinutes(nextAlarmTimeInMillis)
//        val days = (nextAlarmTimeInMillis / (1000 * 60 * 60)) % 24
//        val hours= (nextAlarmTimeInMillis / (1000 * 60)) % 60
//        val minutes= (nextAlarmTimeInMillis / 1000) % 60

        val nextAlarmDate = Date(nextAlarmTriggerTime)
        val dateNow = Date()
        val diffMilliSec = nextAlarmDate.time - dateNow.time

        val seconds = diffMilliSec / 1000
        val minutes = (seconds / 60) + 1
        val hours = minutes / 60
        val days = hours / 24

        when {
            days != 0L -> context.showToast("next alarm scheduled in $days days, $hours hours and $minutes minutes.")
            hours != 0L -> context.showToast("next alarm scheduled in $hours hours and $minutes minutes.")
            else -> context.showToast("next alarm $minutes minutes.")
        }
    }

    private fun setAlarm(alarm: Alarm, context: Context) {
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

        // if time have past go to next day
        if (System.currentTimeMillis() > alarm.time.getTimeInMillis())
            alarm.miliTime = alarm.miliTime + AlarmManager.INTERVAL_DAY

        if (alarm.days == null || alarm.days?.size == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmMgr?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.miliTime,
                    pIntent
                )
            } else {
                alarmMgr?.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarm.miliTime,
                    pIntent
                )
            }
        } else {
            alarmMgr?.setRepeating(
                AlarmManager.RTC_WAKEUP,
                alarm.miliTime,
                AlarmManager.INTERVAL_DAY,
                pIntent
            )
        }
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