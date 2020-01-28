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
            var startIntent = Intent(context, AlarmyNotificationService::class.java)
            startIntent.putExtra(FOREGROUND_INPUT_EXTRA, message)
            startIntent.putExtra(ALARM_DATA_EXTRA, alarm)

            if (alarm == null) {
                startIntent = Intent(context, AlarmyActivity::class.java)
            }

            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, AlarmyNotificationService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        startForeground(FOREGROUND_NOTIFICATION_ID, createNotification(intent))

        val stringLocation = intent?.getStringExtra(ALARM_DATA_EXTRA)
        val type = object : TypeToken<Alarm>() {}.type
        val alarm: Alarm = Gson().fromJson(stringLocation, type)

        if (alarm.vibration) vibrate(applicationContext)
        startMediaPlayer(applicationContext)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
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
        val alarmyIntent = Intent(this, AlarmyActivity::class.java)
        alarmyIntent.putExtra(ALARM_DATA_EXTRA, intent?.getStringExtra(ALARM_DATA_EXTRA))

        val stackBuilder: TaskStackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(AlarmyActivity::class.java)
        stackBuilder.addNextIntent(alarmyIntent)

        val pendingIntent: PendingIntent = stackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT)
        val actionBuilder =
            NotificationCompat.Action.Builder(R.drawable.ic_add_alarm, "Action", pendingIntent)

        return NotificationCompat.Builder(
            this,
            FOREGROUND_NOTIFICATION_CHANNEL_ID
        )
            .setContentTitle(FOREGROUND_NOTIFICATION_TITLE)
            .setContentText(intent?.getStringExtra(FOREGROUND_INPUT_EXTRA))
            .setSmallIcon(R.drawable.ic_add_alarm)
            .setContentIntent(pendingIntent)
            .addAction(actionBuilder.build())
            .build()
    }
}
