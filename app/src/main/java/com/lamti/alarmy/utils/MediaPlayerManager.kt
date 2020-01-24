package com.lamti.alarmy.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build

object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null

    fun startMediaPlayer(context: Context) {
        mediaPlayer = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            mediaPlayer!!.setAudioAttributes(audioAttributes)
        } else {
            mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_RING)
        }

        mediaPlayer!!.setDataSource(context, getAlarmUri()!!)
        try {
            mediaPlayer!!.prepare()
        } catch (e: Exception) {
        }

        if (!mediaPlayer!!.isPlaying)
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

    fun stopMediaPlayer() = mediaPlayer!!.stop()
}
