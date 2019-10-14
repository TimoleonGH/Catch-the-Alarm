package com.lamti.alarmy.ui

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.airbnb.lottie.LottieAnimationView
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import kotlinx.android.synthetic.main.list_item_alarm.view.*

class AlarmsAdapter(private var interaction: Interaction? = null, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setInteraction(listener: Interaction) {
        interaction = listener
    }

    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Alarm>() {

        override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
            return oldItem == newItem
        }

    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

//        return AlarmViewHolder.from(parent)
        return AlarmViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_alarm,
                parent,
                false
            ),
            interaction
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AlarmViewHolder -> {
                holder.bind(differ.currentList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Alarm>) {
        differ.submitList(list)
    }

    fun getList(): List<Alarm> = differ.currentList

    class AlarmViewHolder (itemView: View, private val interaction: Interaction?) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Alarm) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            itemView.day_night_switchLAV.setOnClickListener {
                interaction?.onSwitchClicked(adapterPosition, item)
            }

            itemView.daysTV.text = item.days.toString()
            itemView.timeTV.text = item.time

            changeCardBackgroundColor(itemView.card_view_backgroundCL, item.isOn)
            setSwitchProgress(itemView.day_night_switchLAV, item.isOn)
        }

        private fun setSwitchProgress(dayNightSwitchLav: LottieAnimationView, on: Boolean) {
            if (on)
                dayNightSwitchLav.progress = 0.5f
            else
                dayNightSwitchLav.progress = 0f
        }

        private fun changeCardBackgroundColor(cardViewCL: View, flag: Boolean) {

            val firstColor : Int = if ( flag )
                Color.parseColor("#FABF45")
            else
                Color.parseColor("#33147A")

            cardViewCL.setBackgroundColor(firstColor)
        }

        private fun switchAnimation(animationView: LottieAnimationView, isOn: Boolean) {
            /*animationView.apply {
                speed = -speed
                setMinAndMaxFrame(0, 30)
                if (!isAnimating) {
                    playAnimation()
                }
            }*/
            /*animationView.apply {
                progress = if (isOn)
                    0.5f
                else
                    0f
            }*/

            animationView.apply {
                if (isOn) {
                    itemView.day_night_switchLAV.setMinAndMaxProgress(0.5f, 0.7f)
                    Log.d("APAPAPA", "isOn")
                } else {
                    itemView.day_night_switchLAV.setMinAndMaxProgress(0.3f, 0.5f)
                    Log.d("APAPAPA", "isNOTon")
                }

                itemView.day_night_switchLAV.playAnimation()
            }
        }

        private fun changeState(itemView: View, isOn: Boolean) {
            if (isOn)
                itemView.day_night_switchLAV.setMinAndMaxProgress(0.2f, 0.5f)
            else
                itemView.day_night_switchLAV.setMinAndMaxProgress(0.55f, 1f)
            itemView.day_night_switchLAV.playAnimation()
        }

        private fun animateCardBackgroundColor(view: View, flag: Boolean) {

            val firstColor : Int
            val secondColor : Int

            if ( flag ) {
                firstColor = Color.parseColor("#33147A")
                secondColor = Color.parseColor("#FABF45")
            } else {
                firstColor = Color.parseColor("#FABF45")
                secondColor = Color.parseColor("#33147A")
            }

            /*if ( flag ) {
                firstColor = ContextCompat.getColor(context, R.color.colorYellowDark)
                secondColor = ContextCompat.getColor(context, R.color.colorPurpleDark)
            } else {
                firstColor = ContextCompat.getColor(context, R.color.colorPurpleDark)
                secondColor = ContextCompat.getColor(context, R.color.colorYellowDark)
            }*/

            val objectAnimator = ObjectAnimator.ofObject(
                view, "backgroundColor", ArgbEvaluator(),
                firstColor,
                secondColor
            )

//            objectAnimator.repeatCount = 1
//            objectAnimator.repeatMode = ValueAnimator.REVERSE

            objectAnimator.duration = 350
            objectAnimator.start()
        }

        companion object {
            fun from(parent: ViewGroup): AlarmViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.list_item_alarm, parent, false)

                return AlarmViewHolder(view, null)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Alarm)
        fun onSwitchClicked(position: Int, item: Alarm)
    }
}