package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetItemBinding
import org.cxct.sportlottery.network.third_game.money_transfer.GameData

class SpinnerOutAdapter (private val defaultCheckedCode: String?, private val checkedListener: ItemCheckedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mNowCheckedPos:Int? = null
    var dataList = listOf<GameData>()
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

        if (data.code == defaultCheckedCode && mNowCheckedPos == null) {
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

    class ItemViewHolder private constructor(val binding: ContentBottomSheetItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GameData) {

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
                val binding = ContentBottomSheetItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: GameData) -> Unit) {
        fun onChecked(isChecked: Boolean, data: GameData) = checkedListener(isChecked, data)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

}

