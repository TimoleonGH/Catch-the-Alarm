package com.lamti.alarmy.ui

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.domain.managers.AlarmyManager
import com.lamti.alarmy.domain.services.AlarmyNotificationService
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.ui.main_activity.AlarmVieModel
import com.lamti.alarmy.domain.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.domain.managers.MediaPlayerManager.startMediaPlayer
import com.lamti.alarmy.domain.managers.MediaPlayerManager.stopMediaPlayer
import com.lamti.alarmy.domain.managers.VibrationManager.stopVibration
import com.lamti.alarmy.domain.managers.VibrationManager.vibrate
import com.lamti.alarmy.domain.utils.randomPositionAnimation
import kotlinx.android.synthetic.main.activity_alarmy.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

private const val GAME_COUNTER_GOAL = 2
private const val ALARM_TITLE = "Wake App man! Work as it is still day..."

class AlarmyActivity : AppCompatActivity() {
    private val alarmyManager = AlarmyManager
    private val alarmVieModel: AlarmVieModel by viewModel()

    private lateinit var alarm: Alarm
    private var gameCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullscreen()
        setContentView(R.layout.activity_alarmy)

        initAll()
        clickListeners()
    }

    private fun setFullscreen() {
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    private fun initAll() {
        getAlarm()
        setAlarmTitle()
        setDate()
        pulseAnimation()
        startMediaPlayer(applicationContext)
        setVibration()
        setGame()
        setSnooze()
    }

    private fun getAlarm(): Alarm {
        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        return Gson().fromJson(stringLocation, type)
    }

    private fun setAlarmTitle() {
        if (alarm.message.isEmpty())
            alarmy_text_TV.text = ALARM_TITLE
        else
            alarmy_text_TV.text = alarm.message
    }

    private fun setDate() {
        val dateFormat =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        alarmy_title_TV.text = dateFormat.format(Calendar.getInstance().time)
    }

    private fun pulseAnimation() {
        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
            alarmy_stop_alarm_IV,
            PropertyValuesHolder.ofFloat("scaleX", 1.2f),
            PropertyValuesHolder.ofFloat("scaleY", 1.2f)
        )
        scaleDown.duration = 310
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.REVERSE

        layout_ripple_pulse.startRippleAnimation()
    }

    private fun setVibration() {
        if (alarm.vibration)
            vibrate(applicationContext)
    }

    private fun setGame() {
        if (alarm.game) game()
    }

    private fun clickListeners() {
        alarmy_stop_alarm_IV.setOnClickListener {
            stopGameAlarmOrGamelessAlarm()
        }

        alarmy_snooze_TV.setOnClickListener {
            alarmy_snooze_TV.text = "Snooze? What are you, a kitty?"
            alarmy_snooze_TV.backgroundTintList = resources.getColorStateList(R.color.colorRed)
        }
    }

    private fun stopGameAlarmOrGamelessAlarm() {
        if (alarm.game) {
            stopGameAlarm()
        } else {
            stopAlarmActivity()
        }
    }

    private fun stopGameAlarm() {
        if (gameCounter >= GAME_COUNTER_GOAL) {
            stopAlarmActivity()
        } else {
            gameCounter++
            Toast.makeText(this@AlarmyActivity, "pressed: $gameCounter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAlarmActivity() {
        layout_ripple_pulse.stopRippleAnimation()
        stopVibration()
        stopMediaPlayer()
        stopAlarm()
        closeApp()
    }

    private fun stopAlarm() {
        if (alarm.intDays.isNullOrEmpty()) {
            cancelAlarm()
            AlarmyNotificationService.stopService(this)
        } else {
            AlarmyNotificationService.stopService(this)
            restartRepeatingAlarm()
        }
    }

    private fun cancelAlarm() {
        alarm.isOn = false
        alarmVieModel.updateAlarm(alarm).invokeOnCompletion {
            alarmyManager.cancelAlarm(alarm, applicationContext)
        }
    }

    private fun restartRepeatingAlarm() {
        // Check if repeating alarm has only one day to fix the above bug
        if (alarm.intDays?.size == 1) {
            // start next alarm after one minute (and something) to prevent from ringing again
            Handler().postDelayed(Runnable {
                if (alarm.isOn)
                    alarmyManager.addAlarm(alarm, this, true)
            }, 62 * 1000)
        } else
            alarmyManager.addAlarm(alarm, this, true)
    }

    private fun closeApp() = finishAndRemoveTask()

    private fun game() =
        alarmy_stop_alarm_IV.randomPositionAnimation(alarmy_activity_root_CL, windowManager)

    private fun setSnooze() {
        if (alarm.snooze)
            alarmy_snooze_TV.visibility = View.VISIBLE
        else
            alarmy_snooze_TV.visibility = View.INVISIBLE
    }
}
