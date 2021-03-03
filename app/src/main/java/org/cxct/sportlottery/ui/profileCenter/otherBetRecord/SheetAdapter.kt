package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_other_bet_record_item.view.*
import kotlinx.android.synthetic.main.fragment_other_bet_record.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetOtherBetRecordItemBinding
import org.cxct.sportlottery.ui.base.BaseSocketFragment

data class SheetData(val firmType: String?, val showName: String?) {
    var isChecked = false
}

class SheetAdapter (private val defaultCheckedCode: String?, private val checkedListener: ItemCheckedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNowCheckedPos:Int? = null
    var dataList = listOf<SheetData>()
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

                setSingleChecked(holder.binding.checkbox, position)

                holder.bind(data)
            }
        }
    }

    private fun setSingleChecked(checkbox: CheckBox, position: Int) {
        val data = dataList[position]

        if (data.firmType == defaultCheckedCode && mNowCheckedPos == null) {
            data.isChecked = true
            mNowCheckedPos = position
        }

        checkbox.setOnClickListener {
            val previousPosition = mNowCheckedPos

            if (previousPosition != null) {
                dataList[previousPosition].isChecked = false
                notifyItemChanged(previousPosition)
            }

            mNowCheckedPos = position
            checkbox.isChecked = true
            data.isChecked = true
            checkedListener.onChecked(checkbox.isChecked, data)

            notifyItemChanged(position)
        }
    }

    class ItemViewHolder private constructor(val binding: ContentBottomSheetOtherBetRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SheetData) {

            itemView.apply {
                if (data.isChecked) {
                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
                } else {
                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                }
            }
            binding.item = data
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContentBottomSheetOtherBetRecordItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: SheetData) -> Unit) {
        fun onChecked(isChecked: Boolean, data: SheetData) = checkedListener(isChecked, data)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

}