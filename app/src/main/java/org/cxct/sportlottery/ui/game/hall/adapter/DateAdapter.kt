package org.cxct.sportlottery.ui.game.hall.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_date_v4.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.ui.game.data.Date
import org.cxct.sportlottery.util.LanguageManager

class DateAdapter : RecyclerView.Adapter<DateAdapter.ViewHolder>() {

    var data = listOf<Date>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dateListener: DateListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, dateListener)
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: Date, dateListener: DateListener?) {
            when (item.isDateFormat) {
                true -> setupCalendarView(item)
                false -> setupOtherView(item)
            }
            itemView.isSelected = item.isSelected
            itemView.setOnClickListener {
                dateListener?.onClick(item)
            }
        }

        enum class DateSplitPart(val part: Int) {
            MONTH_DATE(1), WEEKDAY(2)
        }

        private fun setupCalendarView(item: Date) {
            itemView.date_text_other.visibility = View.GONE

            //格式: 2021-December-28-Tue
            itemView.date_text_date.text = item.display.split("-")[DateSplitPart.MONTH_DATE.part]

            when (LanguageManager.getSelectLanguage(itemView.context)) {
                LanguageManager.Language.VI -> {
                    itemView.date_text_week.text =
                        item.display.split("-")[DateSplitPart.WEEKDAY.part]
                            .replace("Th", "Thứ")
                }
                LanguageManager.Language.EN -> {
                    itemView.date_text_week.text =
                        item.display.split("-")[DateSplitPart.WEEKDAY.part]
                            .substring(0, 3)
                }
                else -> {
                    itemView.date_text_week.text =
                        item.display.split("-")[DateSplitPart.WEEKDAY.part]
                }
            }
        }



        private fun setupOtherView(item: Date) {
            itemView.date_text_other.visibility = View.VISIBLE
            itemView.date_text_other.text = item.display
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.itemview_date_v4, parent, false)

                return ViewHolder(view)
            }
        }
    }
}

class DateListener(val clickListener: (date: Date) -> Unit) {
    fun onClick(date: Date) = clickListener(date)
}