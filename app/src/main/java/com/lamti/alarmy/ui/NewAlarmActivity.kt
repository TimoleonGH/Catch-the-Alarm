package com.lamti.alarmy.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.lamti.alarmy.R
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.receivers.AlarmyManager
import com.lamti.alarmy.ui.main_activity.MainVieModel
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.changeIconColor
import kotlinx.android.synthetic.main.activity_new_alarm.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class NewAlarmActivity : AppCompatActivity() {

    private lateinit var alarm: Alarm
    private var updateAlarm = false
    private val decimalFormat = DecimalFormat("00")
    private val mainVieModel: MainVieModel by viewModel()
    private val alarmyManager = AlarmyManager

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
            Alarm(
                0,
                "09:00",
                0L,
                "",
                null,
                null,
                game = true,
                snooze = true,
                vibration = true,
                isOn = true
            )
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
                alarm.time =
                    "${decimalFormat.format(currentHour)}:${decimalFormat.format(currentMinute)}"
            }
        }

        new_alarm_time_picker_TP.setOnTimeChangedListener { view, hourOfDay, minute ->

            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            cal.set(Calendar.MILLISECOND, 0)

            alarm.miliTime = cal.timeInMillis
            alarm.time = "${decimalFormat.format(hourOfDay)}:${decimalFormat.format(minute)}"
        }
    }

    private fun initDaysPicker() {
        if (updateAlarm) {
            if (!alarm.days.isNullOrEmpty())
                new_alarm_day_picker_DPV.setSelectedDaysList(alarm.days!!)
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

        if (updateAlarm) {
            new_alarm_add_B.text = getString(R.string.update)
            new_alarm_delete_IV.visibility = View.VISIBLE
        } else {
            new_alarm_add_B.text = getString(R.string.add)
            new_alarm_delete_IV.visibility = View.GONE
        }
    }

    private fun clickListeners() {
        new_alarm_delete_IV.setOnClickListener {
            alarmyManager.cancelAlarm(alarm, applicationContext)
            mainVieModel.deleteAlarm(alarm)
            finish()
        }

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
                alarm.isOn = true
                mainVieModel.updateAlarm(alarm).invokeOnCompletion {
                    alarmyManager.addAlarm(alarm, applicationContext)
//                    new_alarm_root_CL.showSnackBar("Alarm updated")
                    finish()
                }
            } else {
                mainVieModel.insert(alarm).invokeOnCompletion {
                    alarm.id = mainVieModel.insertedAlarmID.toInt()
                    alarmyManager.addAlarm(alarm, applicationContext)
//                    new_alarm_root_CL.showSnackBar("Alarm added")
                    finish()
                }
            }
        }
    }

    private fun addSelectedDaysToNewAlarm() {

        val selectedDays = new_alarm_day_picker_DPV.getSelectedDaysList()
        val selectedIntDays = ArrayList<Int>()
        val selectedStringDays = ArrayList<String>()

        selectedDays.forEach {
            when (it) {
                "Sunday" -> selectedIntDays.add(1)
                "Monday" -> selectedIntDays.add(2)
                "Tuesday" -> selectedIntDays.add(3)
                "Wednesday" -> selectedIntDays.add(4)
                "Thursday" -> selectedIntDays.add(5)
                "Friday" -> selectedIntDays.add(6)
                "Saturday" -> selectedIntDays.add(7)
            }
        }

        selectedIntDays.sort()
        alarm.intDays = selectedIntDays

        selectedIntDays.forEach {
            when (it) {
                Calendar.SUNDAY -> selectedStringDays.add("Sunday")
                Calendar.MONDAY -> selectedStringDays.add("Monday")
                Calendar.TUESDAY -> selectedStringDays.add("Tuesday")
                Calendar.WEDNESDAY -> selectedStringDays.add("Wednesday")
                Calendar.THURSDAY -> selectedStringDays.add("Thursday")
                Calendar.FRIDAY -> selectedStringDays.add("Friday")
                Calendar.SATURDAY -> selectedStringDays.add("Saturday")
                else -> ""
            }

        }

        alarm.days = selectedStringDays

        alarm.intDays?.forEachIndexed { index, i ->
            Log.d("ANALA", "index: $index, day: $i")
        }
    }

    private fun addMessageToNewAlarm() {
        alarm.message =
            if (new_alarm_message_ET.text.isNullOrEmpty()) "" else new_alarm_message_ET.text.toString()
    }

}
