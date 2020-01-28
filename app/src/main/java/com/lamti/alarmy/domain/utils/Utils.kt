package com.lamti.alarmy.domain.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.snackbar.Snackbar
import com.lamti.alarmy.data.models.Alarm
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.util.*

const val ALARM_DATA_EXTRA = "ALARM_DATA_EXTRA"

fun View.showSnackBar(text: String) {
    val snack = Snackbar.make(this, text, Snackbar.LENGTH_SHORT)
    snack.animationMode = Snackbar.ANIMATION_MODE_FADE
    snack.show()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.redirectTo(goClass: Class<*>, alarm: Alarm? = null) {
    val intent = Intent(this, goClass)
    if ( alarm != null ) {
        val gson = Gson()
        val type = object : TypeToken<Alarm>() {}.type
        val json = gson.toJson(alarm, type)
        intent.putExtra(ALARM_DATA_EXTRA, json)
    }
    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
}

fun View.scaleAnimation( startScale: Float = 0f, endScale: Float = 1f) {
    val anim = ScaleAnimation(
        startScale, endScale,
        startScale, endScale,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    anim.fillAfter = true
    anim.duration = 300
    anim.startTime = 300
    anim.interpolator = FastOutSlowInInterpolator()

    val view: View = this
    view.startAnimation(anim)
}

fun ImageView.changeIconColor( flag: Boolean ) {
    if ( flag )
        this.setColorFilter(Color.parseColor("#0E7BF0"), android.graphics.PorterDuff.Mode.SRC_IN)
    else
        this.setColorFilter(Color.parseColor("#A8ADB1"), android.graphics.PorterDuff.Mode.SRC_IN)
}

fun TextView.changeTextColor(on: Boolean) {
    if ( on ) {
        this.setTextColor(Color.parseColor("#2C3642"))
        this.setTextColor(Color.parseColor("#2C3642"))
    } else {
        this.setTextColor(Color.parseColor("#A8ADB1"))
        this.setTextColor(Color.parseColor("#A8ADB1"))
    }
}

fun View.fadeOut() {
    val view: View = this
    view.animate()
        .alpha(0f)
        .setDuration(150)
        .setStartDelay(75)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
//                    view.visibility = View.GONE
//                    view.alpha = 1f
            }
        })
        .start()
}

fun String.getTimeInMillis(): Long {
    var alarmHour = 0
    var alarmMinute = 0

    try {
        val timeTable = this.split(":")
        alarmHour = timeTable[0].toInt()
        alarmMinute = timeTable[1].toInt()
    } catch (e: Exception) {
        Log.e("ERROR", "Error: ${e.message}")
    }

    val alarmCalendar = Calendar.getInstance()
    alarmCalendar.set(Calendar.HOUR_OF_DAY, alarmHour)
    alarmCalendar.set(Calendar.MINUTE, alarmMinute)
    alarmCalendar.set(Calendar.SECOND, 0)
    alarmCalendar.set(Calendar.MILLISECOND, 0)

    return alarmCalendar.timeInMillis
}

fun View.randomPositionAnimation(
    root: ConstraintLayout,
    windowManager: WindowManager,
    duration: Long = 650
) {
    val view = this

    val anim: Animation = AlphaAnimation(0.0f, 1.0f)
    anim.duration = duration
    anim.startOffset = 0
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = Animation.INFINITE
    view.startAnimation(anim)

    anim.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {
            view.setRandomXYPosition(root, windowManager)
        }

        override fun onAnimationEnd(p0: Animation?) {

        }

        override fun onAnimationStart(p0: Animation?) {

        }

    })
}

private fun View.setRandomXYPosition(root: ConstraintLayout, windowManager: WindowManager) {
    val set = ConstraintSet()
    set.clone(root)
    set.setTranslationX(this.id, getRandomX(windowManager) - this.x)
    set.setTranslationY(this.id, getRandomY(windowManager) - this.y)
    set.applyTo(root)
}


private fun getRandomX(windowManager: WindowManager): Float {
    val x = Random().nextInt(getScreenWidthSize(windowManager)) / 2
    return x.toFloat()
}

private fun getRandomY(windowManager: WindowManager): Float {
    val y = Random().nextInt(getScreenHeightSize(windowManager)) / 2
    return y.toFloat()
}

private fun getScreenWidthSize(windowManager: WindowManager): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

private fun getScreenHeightSize(windowManager: WindowManager): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}
