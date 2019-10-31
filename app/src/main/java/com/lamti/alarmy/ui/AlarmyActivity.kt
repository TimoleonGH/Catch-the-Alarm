package com.lamti.alarmy.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.lamti.alarmy.R
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.receivers.AlarmyManager
import com.lamti.alarmy.ui.main_activity.MainVieModel
import com.lamti.alarmy.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.utils.showToast
import kotlinx.android.synthetic.main.activity_alarmy.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.*
import android.util.DisplayMetrics
import android.view.animation.TranslateAnimation


class AlarmyActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var alarm: Alarm
    private val alarmyManager = AlarmyManager
    private val mainVieModel: MainVieModel by viewModel()

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

        startMediaPlayer(getAlarmUri())
        if (alarm.vibration) vibrate()
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
            finish()

            if (alarm.intDays.isNullOrEmpty()) {
                alarm.isOn = false
                mainVieModel.updateAlarm(alarm).invokeOnCompletion {
                    alarmyManager.cancelAlarm(alarm, applicationContext)
                }
            } else {
                alarmyManager.addAlarm(alarm, this, true)
                /*Handler().postDelayed ({
                    alarmyManager.addAlarm(alarm, this, true)
                }, 60 * 1000)*/
            }
        }

        alarmy_snooze_TV.setOnClickListener {
            this.showToast("Snooze is coming soon enough!")
        }
    }



    // ~~~~~~~~~~ SOUND ~~~~~~~~~~
    private fun startMediaPlayer(uri: Uri?) {
        mediaPlayer = MediaPlayer()
//        val uri = Uri.parse("android.resource://com.lamti.youvegotmessage/" + R.raw.send_message_sound)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            mediaPlayer!!.setAudioAttributes(audioAttributes)
        } else {
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_RING)
        }

        mediaPlayer!!.setDataSource(this, uri)
        try {
            mediaPlayer!!.prepare()
        } catch (e: Exception) {
        }

        mediaPlayer!!.start()
    }

    private fun getAlarmUri(): Uri? {
        var alert: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        if (alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (alert == null)
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        return alert
    }

    private fun stopMediaPlayer() = mediaPlayer!!.stop()



    // ~~~~~~~~~~ VIBRATION ~~~~~~~~~~
    private fun vibrate() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mVibratePattern = longArrayOf(0, 750, 0, 0, 0, 0, 10, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createWaveform(mVibratePattern, 0))
//            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(mVibratePattern, 0)
        }
    }

    private fun stopVibration() = vibrator?.cancel()



    // ~~~~~~~~~~ GAME ~~~~~~~~~~
    private fun game() {

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val r = Random()
        val translationX = r.nextInt(width).toFloat()
        val translationY = r.nextInt(height).toFloat()

        val anim = TranslateAnimation( alarmy_stop_alarm_IV.x, translationX , alarmy_stop_alarm_IV.y, translationY )
        anim.duration = 1000
        anim.fillAfter = true

        alarmy_stop_alarm_IV.startAnimation(anim)
    }


    // ~~~~~~~~~~ SNOOZE ~~~~~~~~~~
    private fun snooze() {
        if (alarm.snooze)
            alarmy_snooze_TV.visibility = View.VISIBLE
        else
            alarmy_snooze_TV.visibility = View.INVISIBLE
    }
}
