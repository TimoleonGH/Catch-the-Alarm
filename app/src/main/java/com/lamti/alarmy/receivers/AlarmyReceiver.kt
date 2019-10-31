package com.lamti.alarmy.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.ui.AlarmyActivity
import com.lamti.alarmy.ui.main_activity.MainActivity
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class AlarmyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val cAlarm = getAlarm(intent)
        val maxDuration= TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS)
        val duration = abs(System.currentTimeMillis() - cAlarm.miliTime)

//        if (duration < maxDuration)
//            launchMainActivity(context)
//        else
        launchAlarmyActivity(context, cAlarm)
    }

    private fun getAlarm(intent: Intent?): Alarm {
        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        return Gson().fromJson(stringLocation, type)
    }

    private fun launchAlarmyActivity(context: Context?, alarm: Alarm) {
        val alarmyIntent = Intent(context, AlarmyActivity::class.java)
        val type = object : TypeToken<Alarm>() {}.type
        val json = Gson().toJson(alarm, type)
        alarmyIntent.putExtra(ALARM_DATA_EXTRA, json)
        context?.startActivity(alarmyIntent)
    }

    private fun launchMainActivity(context: Context?) = context?.startActivity(Intent(context, MainActivity::class.java))

}