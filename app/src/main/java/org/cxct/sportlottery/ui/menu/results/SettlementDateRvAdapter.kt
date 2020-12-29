package org.cxct.sportlottery.ui.menu.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R

class SettlementDateRvAdapter : RecyclerView.Adapter<DateItemViewHolder>() {
    private var mDateSelectedList: MutableList<Boolean> = mutableListOf()
    var mDateList: MutableList<String> = mutableListOf()
        set(value) {
            field = value
            mDateSelectedList = MutableList(mDateList.size) { false }
            mDateSelectedList[0] = true
            notifyDataSetChanged()
        }
    var refreshDateListener: RefreshDateListener? = null
        set(listener) {
            field = listener
        }

    interface RefreshDateListener {
        fun refreshDate(date: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.settlement_date_item, parent, false)
        return DateItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDateList.size
    }

    override fun onBindViewHolder(holder: DateItemViewHolder, position: Int) {
        holder.apply {
            ctvItem.text = mDateList[adapterPosition]
            ctvItem.isChecked = mDateSelectedList[adapterPosition]
            ctvItem.setOnClickListener {
                ctvItem.isChecked = true
                if (!mDateSelectedList[adapterPosition]) {
                    mDateSelectedList[mDateSelectedList.indexOf(true)] = false
                    mDateSelectedList[adapterPosition] = !(mDateSelectedList[adapterPosition])
                    refreshDateListener?.refreshDate(adapterPosition)
                    notifyDataSetChanged()
                }
            }
        }
    }
}


class DateItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val ctvItem: CheckBox = itemView.findViewById(R.id.ctv_item)
}