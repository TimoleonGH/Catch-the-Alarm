package com.lamti.alarmy.domain.managers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object VibrationManager {
    private var vibrator: Vibrator? = null

    fun vibrate(context: Context) {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val mVibratePattern = longArrayOf(0, 750, 0, 0, 0, 0, 10, 0, 0, 0)
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(VibrationEffect.createWaveform(mVibratePattern, 0))
//            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(mVibratePattern, 0)
        }
    }

    fun stopVibration() = vibrator?.cancel()
}
