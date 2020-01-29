package com.lamti.alarmy.domain.managers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationManager {
    private var vibrator: Vibrator? = null

    fun vibrate(context: Context) {
        initVibrator(context)
        val vibratorPattern = setVibratorPattern()
        setVibratorForAndroidVersion(vibratorPattern)
    }

    private fun initVibrator(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun setVibratorPattern(): LongArray {
        return longArrayOf(0, 750, 0, 0, 0, 0, 10, 0, 0, 0)
    }

    private fun setVibratorForAndroidVersion(vibratorPattern: LongArray) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createWaveform(vibratorPattern, 0))
        } else {
            vibrator?.vibrate(vibratorPattern, 0)
        }
    }

    fun stopVibration() = vibrator?.cancel()
}
