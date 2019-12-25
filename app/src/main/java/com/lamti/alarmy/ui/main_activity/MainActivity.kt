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
import android.app.AlarmManager
import android.content.Context
import android.view.ViewAnimationUtils
import android.os.Handler
import android.view.View.GONE
import android.view.View.VISIBLE
import com.lamti.alarmy.R
import com.lamti.alarmy.receivers.AlarmyManager
import com.lamti.alarmy.ui.NewAlarmActivity
import com.lamti.alarmy.ui.SettingsActivity
import com.lamti.alarmy.utils.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), AlarmAdapter.Interaction {

    private val mainVieModel: MainVieModel by viewModel()
    private val alarmsAdapter : AlarmAdapter by inject { parametersOf(this, this@MainActivity) }
    private val alarmyManager = AlarmyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmsAdapter.setInteraction(this)

        initRecyclerView()
        initObserver()
        addAlarmListener()

        add_alarm_IV.scaleAnimation()
    }

    override fun onResume() {
        super.onResume()
        main_root_CL.visibility = VISIBLE
        main_root_CL.alpha = 1f
    }



    private fun initRecyclerView() {
        alarms_RV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(SpacingItemDecoration( this@MainActivity,60))
            adapter = alarmsAdapter
        }
    }

    private fun initObserver() {
        mainVieModel.allAlarms.observe(this, Observer {result ->
            if ( result.isEmpty() ) {
                val headerAlarm = Alarm(0, "nullara", 0L,"nullara",null, null, game = false,
                    vibration = false, snooze = false, isOn = false)
                mainVieModel.insert(headerAlarm)
            }
            alarmsAdapter.submitList(result)
        })
    }

    private fun addAlarmListener() {
        add_alarm_IV.setOnClickListener {
            revealButton()
        }
    }

    private fun revealButton() {
        add_alarm_IV.elevation = 0f
        add_alarm_IV.alpha = 0f
        reveal_view.visibility = VISIBLE
        reveal_view.alpha = 1f

        val cx = reveal_view.width
        val cy = reveal_view.height
        val startX = (getFabWidth() / 2 + add_alarm_IV.x).toInt()
        val startY = (getFabWidth() / 2 + add_alarm_IV.y).toInt()
        val finalRadius = cx.coerceAtLeast(cy) * 1.2f
        val revealAnimation = ViewAnimationUtils.createCircularReveal(reveal_view, startX, startY, getFabWidth(), finalRadius)

        revealAnimation.duration = 350
        revealAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                delayedStartNextActivity()
                main_root_CL.fadeOut()
            }
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                add_alarm_IV.alpha = 1f

                reveal_view.visibility = GONE
                reveal_view.alpha = 0f
            }
        })

        revealAnimation.start()
    }

    private fun getFabWidth(): Float {
        return add_alarm_IV.width.toFloat()
    }

    private fun delayedStartNextActivity() {
        Handler().postDelayed(Runnable {
            redirectTo(NewAlarmActivity::class.java)
        }, 100)
    }

    override fun onItemSelected(position: Int, item: Alarm) {
        redirectTo(NewAlarmActivity::class.java, item)
    }

    override fun onItemChecked(position: Int, item: Alarm) {
        item.isOn = !item.isOn
        mainVieModel.updateAlarm(item).invokeOnCompletion {
            alarmsAdapter.notifyItemChanged(position)
            alarmyManager.updateAlarm(item, applicationContext)
        }
    }

    override fun onSettingsClicked() {
//        mainVieModel.deleteAll()
        redirectTo(SettingsActivity::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainVieModel
    }
}
