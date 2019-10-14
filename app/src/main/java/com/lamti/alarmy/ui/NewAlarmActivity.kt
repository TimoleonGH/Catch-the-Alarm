package com.lamti.alarmy.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.lamti.alarmy.R
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.changeIconColor
import com.lamti.alarmy.utils.showSnackBar
import kotlinx.android.synthetic.main.activity_new_alarm.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.util.*

class NewAlarmActivity : AppCompatActivity() {

    private lateinit var alarm: Alarm
    private var updateAlarm = false
    private val decimalFormat = DecimalFormat("00")
    private val mainVieModel: MainVieModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_alarm)

        initAlarm()
        initTimePicker()
        initDaysPicker()
        initMessageET()
        initButtonIcons()
        clickListeners()
    }

    private fun initAlarm() {
        val stringLocation = intent.getStringExtra(ALARM_DATA_EXTRA)
        alarm = if (stringLocation != null) {
            updateAlarm = true
            val type = object : TypeToken<Alarm>() {}.type
            Gson().fromJson(stringLocation, type)
        } else {
            updateAlarm = false
            Alarm(0, "09:00", "", null, game = true, snooze = true, vibration = true, isOn = true)
        }
    }

    private fun initTimePicker() {
        new_alarm_time_picker_TP.setIs24HourView(true)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

        if (Build.VERSION.SDK_INT >= 23) {
            if (updateAlarm) {
                val timeTable = alarm.time.split(":")
                new_alarm_time_picker_TP.hour = timeTable[0].toInt()
                new_alarm_time_picker_TP.minute = timeTable[1].toInt()
            } else {
                new_alarm_time_picker_TP.hour = currentHour
                new_alarm_time_picker_TP.minute = currentMinute
                alarm.time = "${decimalFormat.format(currentHour)}:${decimalFormat.format(currentMinute)}"
            }
        }

        new_alarm_time_picker_TP.setOnTimeChangedListener { view, hourOfDay, minute ->
            alarm.time = "${decimalFormat.format(hourOfDay)}:${decimalFormat.format(minute)}"
        }
    }

    private fun initDaysPicker() {
        if (updateAlarm) {
            if (!alarm.days.isNullOrEmpty()) {
                new_alarm_day_picker_DPV.setSelectedDaysList(alarm.days!!)
                Log.d("PAPA","alarm: ${alarm.days}")
            } else
                Log.d("PAPA","null or empty")
        }
    }

    private fun initMessageET() {
        if (updateAlarm && alarm.message.isNotEmpty())
            new_alarm_message_ET.setText(alarm.message)
    }

    private fun initButtonIcons() {
        new_alarm_snooze_IV.changeIconColor(alarm.snooze)
        new_alarm_vibrate_IV.changeIconColor(alarm.vibration)
        new_alarm_game_IV.changeIconColor(alarm.game)

        if (updateAlarm)
            new_alarm_add_B.text = "update"
        else
            new_alarm_add_B.text = "add"
    }

    private fun clickListeners() {
        new_alarm_snooze_IV.setOnClickListener {
            alarm.snooze = !alarm.snooze
            new_alarm_snooze_IV.changeIconColor(alarm.snooze)
        }

        new_alarm_vibrate_IV.setOnClickListener {
            alarm.vibration = !alarm.vibration
            new_alarm_vibrate_IV.changeIconColor(alarm.vibration)
        }

        new_alarm_game_IV.setOnClickListener {
            alarm.game = !alarm.game
            new_alarm_game_IV.changeIconColor(alarm.game)
        }

        new_alarm_cancel_B.setOnClickListener {
            onBackPressed()
        }

        new_alarm_add_B.setOnClickListener {
            addSelectedDaysToNewAlarm()
            addMessageToNewAlarm()

            if (updateAlarm) {
                mainVieModel.updateAlarm(alarm).invokeOnCompletion {
                    new_alarm_root_CL.showSnackBar("Alarm updated")
                    finish()
                }
            } else {
                mainVieModel.insert(alarm).invokeOnCompletion {
                    new_alarm_root_CL.showSnackBar("Alarm added")
                    finish()
                }
            }

        }
    }

    private fun addSelectedDaysToNewAlarm() {
        alarm.days = new_alarm_day_picker_DPV.getSelectedDaysList()
    }

    private fun addMessageToNewAlarm() {
        alarm.message = if (new_alarm_message_ET.text.isNullOrEmpty()) "" else new_alarm_message_ET.text.toString()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
