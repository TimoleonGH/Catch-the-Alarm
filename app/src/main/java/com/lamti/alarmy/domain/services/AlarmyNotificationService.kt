package com.lamti.alarmy.domain.services

import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import com.lamti.alarmy.domain.managers.MediaPlayerManager.startMediaPlayer
import com.lamti.alarmy.domain.managers.VibrationManager.vibrate
import com.lamti.alarmy.domain.utils.ALARM_DATA_EXTRA
import com.lamti.alarmy.ui.AlarmyActivity

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
        startFullscreenForegroundNotification(intent)
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                FOREGROUND_NOTIFICATION_CHANNEL_ID,
                FOREGROUND_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    private fun startFullscreenForegroundNotification(intent: Intent?) {
        startForeground(FOREGROUND_NOTIFICATION_ID, createFullscreenNotification(intent))
    }

    private fun createFullscreenNotification(intent: Intent?): Notification {
        val fullscreenIntent = createFullScreenIntent()
        val pendingIntent = createFullScreenPendingIntent(intent, fullscreenIntent)
        return getNotification(intent, pendingIntent)
    }

    private fun createFullScreenIntent(): Intent {
        return Intent(this, AlarmyActivity::class.java)
    }

    private fun createFullScreenPendingIntent(
        intent: Intent?,
        fullScreenIntent: Intent
    ): PendingIntent {
        fullScreenIntent.putExtra(ALARM_DATA_EXTRA, intent?.getStringExtra(ALARM_DATA_EXTRA))
        return PendingIntent.getActivity(this, 0, fullScreenIntent, FLAG_UPDATE_CURRENT)
    }

    private fun getNotification(
        intent: Intent?,
        fullScreenPendingIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(this, FOREGROUND_NOTIFICATION_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentTitle(FOREGROUND_NOTIFICATION_TITLE)
            .setContentText(intent?.getStringExtra(FOREGROUND_INPUT_EXTRA))
            .setSmallIcon(R.drawable.ic_add_alarm)
            .build()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
