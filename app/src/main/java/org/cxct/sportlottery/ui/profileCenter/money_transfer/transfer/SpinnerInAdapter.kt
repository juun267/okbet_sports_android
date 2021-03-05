package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetItem2Binding
import org.cxct.sportlottery.network.third_game.money_transfer.GameDataInPlat

class SpinnerInAdapter (private val defaultCheckedCode: String?, private val checkedListener: ItemCheckedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//        var previousCheckedPosition: Int? = null

    var dataList = mutableListOf<GameDataInPlat>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var mNowCheckedPos:Int? = null

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

            previousPosition?.let {
                dataList[previousPosition].isChecked = false
                notifyItemChanged(it)
            }

            mNowCheckedPos = position
            data.isChecked = true
            checkedListener.onChecked(data.isChecked, data)

            notifyItemChanged(position)
        }
    }

    class ItemViewHolder private constructor(val binding: ContentBottomSheetItem2Binding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: GameDataInPlat) {
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
                val binding = ContentBottomSheetItem2Binding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ItemCheckedListener(val checkedListener: (isChecked: Boolean, data: GameDataInPlat) -> Unit) {
        fun onChecked(isChecked: Boolean, data: GameDataInPlat) = checkedListener(isChecked, data)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}