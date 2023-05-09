package org.cxct.sportlottery.ui2.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_item.view.*
import org.cxct.sportlottery.R


class StatusSheetAdapter(private val checkedListener: ItemCheckedListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPreviousItem: String? = null

    var checkedItemCode: String? = null

    var defaultCheckedCode: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var dataList = listOf<StatusSheetData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ItemViewHolder -> {
                val data = dataList[position]

                setSingleChecked(holder.itemView.checkbox, position)

                holder.bind(data)
            }
        }
    }

    private fun setSingleChecked(checkbox: CheckBox, position: Int) {
        val data = dataList[position]

        if ((data.code == defaultCheckedCode || data.code == null) && mPreviousItem == null) {
            data.isChecked = true
            checkedItemCode = data.code
            mPreviousItem = dataList[position].showName
        }

        checkbox.setOnClickListener {
            var previousPosition: Int? = null
            dataList.forEachIndexed { index, data ->
                if (data.showName == mPreviousItem)
                    previousPosition = index
            }

            if (previousPosition != null) {
                dataList[previousPosition!!].isChecked = false
                notifyItemChanged(previousPosition!!)
            }

            mPreviousItem = dataList[position].showName
            checkbox.isChecked = true
            data.isChecked = true

            checkedListener.onChecked(checkbox.isChecked, data)

            checkedItemCode = data.code

            notifyItemChanged(position)
        }
    }

    class ItemViewHolder private constructor(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(data: StatusSheetData) {
            itemView.apply {
                checkbox.isChecked = data.isChecked
                checkbox.text = data.showName
                checkbox.setBackgroundColor(
                    if (data.isChecked) ContextCompat.getColor(
                        checkbox.context,
                        R.color.color_191919_EEEFF0
                    ) else ContextCompat.getColor(checkbox.context, R.color.color_191919_FCFCFC)
                )
            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val binding = LayoutInflater.from(parent.context)
                    .inflate(R.layout.custom_bottom_sheet_item, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: StatusSheetData) -> Unit) {
        fun onChecked(isChecked: Boolean, data: StatusSheetData) =
            checkedListener(isChecked, data)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

}

data class StatusSheetData(val code: String?, val showName: String?) {
    var isChecked = false
}