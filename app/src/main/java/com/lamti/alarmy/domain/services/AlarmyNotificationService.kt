package com.lamti.alarmy.domain.services

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.ui.AlarmyActivity
import com.lamti.alarmy.domain.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.domain.managers.MediaPlayerManager.startMediaPlayer
import com.lamti.alarmy.domain.managers.VibrationManager.vibrate


class AlarmyNotificationService : Service() {
    companion object {
        private const val FOREGROUND_INPUT_EXTRA = "inputExtra"
        private const val FOREGROUND_NOTIFICATION_TITLE = "Wake app alarm"
        private const val FOREGROUND_NOTIFICATION_CHANNEL = "Wake up Channel"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        private const val FOREGROUND_NOTIFICATION_CHANNEL_ID = "wake_app_alarm_id"

        fun startService(context: Context, message: String, alarm: String?) {
            val startIntent = initStartIntent(context, message, alarm)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, AlarmyNotificationService::class.java)
            context.stopService(stopIntent)
        }

        private fun initStartIntent(context: Context, message: String, alarm: String?): Intent {
            var startIntent = Intent(context, AlarmyNotificationService::class.java)
            startIntent.putExtra(FOREGROUND_INPUT_EXTRA, message)
            startIntent.putExtra(ALARM_DATA_EXTRA, alarm)

            if (alarm == null) {
                startIntent = Intent(context, AlarmyActivity::class.java)
            }

            return startIntent
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, createNotification(intent))
        startVibration(getAlarm(intent))
        startMediaPlayer(applicationContext)
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                FOREGROUND_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(intent: Intent?): Notification {
        val alarmyIntent = createIntent(intent)
        val stackBuilder = getTaskStackBuilder(alarmyIntent)
        val pendingIntent = getPendingIntent(stackBuilder)
        val actionBuilder = getActionBuilder(pendingIntent)
        return buildNotification(intent, pendingIntent, actionBuilder)
    }

    private fun createIntent(intent: Intent?): Intent {
        val alarmyIntent = Intent(this, AlarmyActivity::class.java)
        alarmyIntent.putExtra(ALARM_DATA_EXTRA, intent?.getStringExtra(ALARM_DATA_EXTRA))
        return alarmyIntent
    }

    private fun getTaskStackBuilder(intent: Intent): TaskStackBuilder {
        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(AlarmyActivity::class.java)
        stackBuilder.addNextIntent(intent)
        return stackBuilder
    }

    private fun getPendingIntent(stackBuilder: TaskStackBuilder): PendingIntent {
        return stackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT)
    }

    private fun getActionBuilder(pendingIntent: PendingIntent): NotificationCompat.Action.Builder {
        return NotificationCompat.Action.Builder(
            R.drawable.ic_add_alarm,
            "Close Alarm",
            pendingIntent
        )
    }

    private fun buildNotification(
        intent: Intent?,
        pendingIntent: PendingIntent,
        actionBuilder: NotificationCompat.Action.Builder
    ): Notification {

        return NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(FOREGROUND_NOTIFICATION_TITLE)
            .setContentText(intent?.getStringExtra(FOREGROUND_INPUT_EXTRA))
            .setSmallIcon(R.drawable.ic_add_alarm)
            .setContentIntent(pendingIntent)
            .addAction(actionBuilder.build())
            .build()
    }

    private fun startVibration(alarm: Alarm) {
        if (alarm.vibration)
            vibrate(applicationContext)
    }

    private fun getAlarm(intent: Intent?): Alarm {
        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        return Gson().fromJson(stringLocation, type)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
