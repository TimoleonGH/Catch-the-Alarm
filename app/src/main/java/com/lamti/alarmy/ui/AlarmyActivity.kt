package com.lamti.alarmy.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.AlarmyManager
import com.lamti.alarmy.AlarmyNotificationService
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.ui.main_activity.MainVieModel
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.MediaPlayerManager.startMediaPlayer
import com.lamti.alarmy.utils.MediaPlayerManager.stopMediaPlayer
import com.lamti.alarmy.utils.Vibrator.stopVibration
import com.lamti.alarmy.utils.Vibrator.vibrate
import kotlinx.android.synthetic.main.activity_alarmy.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class AlarmyActivity : AppCompatActivity() {
    private lateinit var alarm: Alarm
    private val alarmyManager = AlarmyManager
    private val mainVieModel: MainVieModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen()
        setContentView(R.layout.activity_alarmy)

        Log.d("APAPA", "extra: ${intent?.getStringExtra(ALARM_DATA_EXTRA)}")
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
                //        View.SYSTEM_UI_FLAG_LOW_PROFILE
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

    private fun initAll() {
        // get alarm
        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        alarm = Gson().fromJson(stringLocation, type)

        if (alarm.message.isEmpty())
            alarmy_text_TV.text = "Wake App man! Work as it is still day..."
        else
            alarmy_text_TV.text = alarm.message

        val dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        alarmy_title_TV.text = dateFormat.format(Calendar.getInstance().time)

        pulseAnimation()

        startMediaPlayer(applicationContext)
        if (alarm.vibration) vibrate(applicationContext)
        if (alarm.game) game()
        snooze()
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
//        scaleDown.start()

        layout_ripple_pulse.startRippleAnimation()
    }

    private fun clickListeners() {
        alarmy_stop_alarm_IV.setOnClickListener {
            layout_ripple_pulse.stopRippleAnimation()
            stopVibration()
            stopMediaPlayer()

            if (alarm.intDays.isNullOrEmpty()) {
                alarm.isOn = false
                mainVieModel.updateAlarm(alarm).invokeOnCompletion {
                    alarmyManager.cancelAlarm(alarm, applicationContext)
                }
                AlarmyNotificationService.stopService(this)
            } else {
                AlarmyNotificationService.stopService(this)
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

            closeApp()
        }

        alarmy_snooze_TV.setOnClickListener {
            alarmy_snooze_TV.text = "Snooze? What are you, a kitty?"
            alarmy_snooze_TV.backgroundTintList = resources.getColorStateList(R.color.colorRed)
        }
    }

    private fun closeApp() {
        finishAndRemoveTask()
    }


    private fun game() {
        alarmy_stop_alarm_IV.fadeAnimation()
    }

    fun View.fadeAnimation1(value: Float, duration: Long) {
        this.animate()
            .alpha(value)
            .setDuration(duration)
            .start()
    }

    fun View.fadeAnimation(duration: Long = 500) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "translationX", getRandomX())
        objectAnimator.repeatMode = ValueAnimator.REVERSE
        objectAnimator.duration = duration
        objectAnimator.start()

        val objectAnimatorY = ObjectAnimator.ofFloat(this, "translationY", getRandomY())
        objectAnimatorY.repeatMode = ValueAnimator.REVERSE
        objectAnimatorY.duration = duration
        objectAnimatorY.start()

        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                objectAnimator.start()
                objectAnimatorY.start()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    private fun getRandomX(): Float {
        val r = Random()
        return r.nextInt(getScreenWidthSize()).toFloat()
    }

    private fun getRandomY(): Float {
        val r = Random()
        return r.nextInt(getScreenHeightSize()).toFloat()
    }

    private fun getScreenWidthSize(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getScreenHeightSize(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


    private fun snooze() {
        if (alarm.snooze)
            alarmy_snooze_TV.visibility = View.VISIBLE
        else
            alarmy_snooze_TV.visibility = View.INVISIBLE
    }
}
