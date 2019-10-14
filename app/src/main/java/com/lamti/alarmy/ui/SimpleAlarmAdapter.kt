package com.lamti.alarmy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.lamti.alarmy.R
import com.lamti.alarmy.data.models.Alarm
import kotlinx.android.synthetic.main.list_item_header.view.*
import kotlinx.android.synthetic.main.list_item_simple_alarm.view.*
import java.util.*
import java.text.SimpleDateFormat
import android.widget.TextView
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.SpannableString
import com.lamti.alarmy.utils.changeIconColor
import com.lamti.alarmy.utils.changeTextColor
import kotlinx.android.synthetic.main.activity_new_alarm.*

class SimpleAlarmAdapter(private var interaction: Interaction? = null, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

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
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_header, parent, false)
                HeaderViewHolder(view, interaction)
            }
            TYPE_ITEM -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_simple_alarm, parent, false)
                SimpleAlarmViewHolder(view, interaction)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SimpleAlarmViewHolder -> {
                holder.bind(differ.currentList[position])
            }
            is HeaderViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_HEADER
            else -> TYPE_ITEM
//            else -> throw IllegalArgumentException("Invalid type of data " + position)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Alarm?>) {
        differ.submitList(list)
    }

    class SimpleAlarmViewHolder(itemView: View, private val interaction: Interaction?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: Alarm) = with(itemView) {
            itemView.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }

            itemView.simple_item_on_S.setOnClickListener {
                interaction?.onItemChecked(adapterPosition, item)
            }

            itemView.simple_item_on_S.isChecked = item.isOn
            itemView.simple_item_time_TV.text = item.time
            itemView.simple_item_repeat_TV.text = getDaysString(item)

            itemView.simple_item_time_TV.changeTextColor(item.isOn)
            itemView.simple_item_repeat_TV.changeTextColor(item.isOn)

            itemView.simple_item_message_IV.changeIconColor(!item.message.isEmpty())
            itemView.simple_item_snooze_IV.changeIconColor(item.snooze)
            itemView.simple_item_vibrate_IV.changeIconColor(item.vibration)
            itemView.simple_item_game_IV.changeIconColor(item.game)
        }

        private fun getDaysString(item: Alarm): String {
            var daysText = ""
            if ( item.days.isNullOrEmpty() ) {
                daysText = "Today"
            } else {
                if (  item.days!!.size > 3) {
                    item.days!!.forEach { day ->
                        daysText += day.substring(IntRange(0,2)) + "  "
                    }
                } else {
                    item.days!!.forEach { day ->
                        daysText += "$day  "
                    }
                }
            }
            return daysText
        }
    }

    class HeaderViewHolder(itemView: View, private val interaction: Interaction?) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind() = with(itemView) {
            itemView.header_content_TV.text = "The good guy knows another road..."
            setDate(itemView.header_title_TV)

            itemView.header_settings_IB.setOnClickListener {
                interaction?.onSettingsClicked()
            }
        }

        @SuppressLint("SimpleDateFormat")
        private fun getCurrentDate(): String {
            val simpleDateFormat = SimpleDateFormat("EEE, d MMM")
            simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val today = Calendar.getInstance().time
            return simpleDateFormat.format(today)
        }

        private fun setDate(textView: TextView) {
            val text = "CatchTheAlarm · ${getCurrentDate()}"
            val substring = "CatchTheAlarm"
            val spannable = SpannableString(text)
            val start = text.indexOf(substring)
            val end = start + substring.length
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#0E7BF0")),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textView.setText(spannable, TextView.BufferType.SPANNABLE)
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Alarm)
        fun onItemChecked(position: Int, item: Alarm)
        fun onSettingsClicked()
    }
}