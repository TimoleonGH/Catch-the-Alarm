package com.lamti.alarmy.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.domain.services.AlarmyNotificationService
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.data.models.HourMinuteModel
import com.lamti.alarmy.ui.AlarmyActivity
import com.lamti.alarmy.ui.main_activity.MainActivity
import com.lamti.alarmy.domain.utils.ALARM_DATA_EXTRA
import java.util.*

class AlarmyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        returnIfContextOrAlarmIsNull(context, intent)
        val alarm = getAlarm(intent) ?: return
        redirectToProperScreen(alarm, context!!)
    }

    private fun returnIfContextOrAlarmIsNull(context: Context?, intent: Intent?) {
        if (context == null || intent == null || getAlarm(intent) == null)
            return
    }

    private fun getAlarm(intent: Intent?): Alarm? {
        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        return if (stringLocation == null)
            null
        else
            Gson().fromJson(stringLocation, type)
    }

    private fun redirectToProperScreen(alarm: Alarm, context: Context) {
        if (checkIfNowTimeEqualsWithAlarmTime(alarm)) {
            startAlarmNotificationService(alarm, context)
        } else {
            startMainActivity(context)
        }
    }

    private fun checkIfNowTimeEqualsWithAlarmTime(alarm: Alarm): Boolean {
        return getAlarmHourMinuteModel(alarm) == getNowHourMinuteModel()
    }

    private fun getAlarmHourMinuteModel(alarm: Alarm): HourMinuteModel {
        val alarmCalendar = Calendar.getInstance()
        alarmCalendar.timeInMillis = alarm.miliTime

        return HourMinuteModel(
            alarmCalendar.get(Calendar.HOUR_OF_DAY),
            alarmCalendar.get(Calendar.MINUTE)
        )
    }

    private fun getNowHourMinuteModel(): HourMinuteModel {
        val nowCalendar = Calendar.getInstance()

        return HourMinuteModel(
            nowCalendar.get(Calendar.HOUR_OF_DAY),
            nowCalendar.get(Calendar.MINUTE)
        )
    }

    private fun startAlarmNotificationService(alarm: Alarm, context: Context) {
        val json = alarmToJsonMapper(alarm)
        AlarmyNotificationService.startService(
            context, "Close alarm", json
        )
    }

    private fun alarmToJsonMapper(alarm: Alarm): String {
        val type = object : TypeToken<Alarm>() {}.type
        return Gson().toJson(alarm, type)
    }

    private fun startMainActivity(context: Context) {
        AlarmyNotificationService.startService(context, "", null)
    }

}
