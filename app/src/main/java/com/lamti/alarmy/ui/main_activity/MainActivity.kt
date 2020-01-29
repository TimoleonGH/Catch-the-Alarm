package com.lamti.alarmy.ui.main_activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.lamti.alarmy.data.models.Alarm
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.ViewAnimationUtils
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.Animation
import com.lamti.alarmy.R
import com.lamti.alarmy.domain.managers.AlarmyManager
import com.lamti.alarmy.ui.NewAlarmActivity
import com.lamti.alarmy.ui.SettingsActivity
import com.lamti.alarmy.domain.utils.*


class MainActivity : AppCompatActivity(), AlarmAdapter.Interaction {

    private val alarmVieModel: AlarmVieModel by viewModel()
    private val alarmsAdapter: AlarmAdapter by inject { parametersOf(this, this@MainActivity) }
    private val alarmyManager = AlarmyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addAlarmInteraction()
        initRecyclerView()
        initObserver()
        addAlarmListener()
        add_alarm_IV.scaleAnimation()
    }

    override fun onResume() {
        super.onResume()
        showMainLayout()
    }

    private fun showMainLayout() {
        main_root_CL.visibility = VISIBLE
        main_root_CL.alpha = 1f
    }

    override fun onItemSelected(position: Int, item: Alarm) {
        redirectTo(NewAlarmActivity::class.java, item)
    }

    override fun onItemChecked(position: Int, item: Alarm) {
        changeAlarmStatus(item)
        updateAlarmState(position, item)
    }

    private fun changeAlarmStatus(item: Alarm) {
        item.isOn = !item.isOn
    }

    private fun updateAlarmState(position: Int, item: Alarm) {
        alarmVieModel.updateAlarm(item).invokeOnCompletion {
            alarmsAdapter.notifyItemChanged(position)
            alarmyManager.updateAlarm(item, applicationContext)
        }
    }

    override fun onSettingsClicked() {
        redirectTo(SettingsActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun addAlarmInteraction() {
        alarmsAdapter.setInteraction(this)
    }

    private fun initRecyclerView() {
        alarms_RV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpacingItemDecoration(this@MainActivity, 60))
            adapter = alarmsAdapter
        }
    }

    private fun initObserver() {
        alarmVieModel.allAlarms.observe(this, Observer { result ->
            if (result.isEmpty()) {
                val headerAlarm = Alarm(
                    0, "nullara", 0L,
                    "nullara", null, null,
                    game = false, vibration = false, snooze = false,
                    isOn = false
                )
                alarmVieModel.insert(headerAlarm)
            }
            alarmsAdapter.submitList(result)
        })
    }

    private fun addAlarmListener() {
        add_alarm_IV.setOnClickListener {
            revealButton()
        }
    }

    // Animations
    private val REVEAL_ANIMATION_DURATION: Long = 350
    private fun revealButton() {
        hideAddAlarmButton()
        showRevealView()
        val revealAnimation = createRevealAnimation()
        revealAnimation.apply {
            setAnimationDuration(this)
            addRevealAnimationListener(this)
            start()
        }

    }

    private fun hideAddAlarmButton() {
        add_alarm_IV.elevation = 0f
        add_alarm_IV.alpha = 0f
    }

    private fun showRevealView() {
        reveal_view.visibility = VISIBLE
        reveal_view.alpha = 1f
    }

    private fun createRevealAnimation(): Animator {
        val cx = reveal_view.width
        val cy = reveal_view.height
        val startX = (getFabWidth() / 2 + add_alarm_IV.x).toInt()
        val startY = (getFabWidth() / 2 + add_alarm_IV.y).toInt()
        val finalRadius = cx.coerceAtLeast(cy) * 1.2f
        return ViewAnimationUtils.createCircularReveal(
            reveal_view,
            startX,
            startY,
            getFabWidth(),
            finalRadius
        )
    }

    private fun getFabWidth(): Float = add_alarm_IV.width.toFloat()

    private fun setAnimationDuration(revealAnimation: Animator) {
        revealAnimation.duration = REVEAL_ANIMATION_DURATION
    }

    private fun addRevealAnimationListener(revealAnimation: Animator) {
        revealAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                delayedStartNextActivity()
                main_root_CL.fadeOut()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                add_alarm_IV.alpha = 1f
                hideRevealView()
            }
        })
    }

    private fun delayedStartNextActivity() {
        Handler().postDelayed(Runnable {
            redirectTo(NewAlarmActivity::class.java)
        }, 100)
    }

    private fun hideRevealView() {
        reveal_view.visibility = GONE
        reveal_view.alpha = 0f
    }
}
