package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetItemBinding
import org.cxct.sportlottery.network.third_game.money_transfer.GameData

class SpinnerOutAdapter (private val checkedListener: ItemCheckedListener) : ListAdapter<GameData, RecyclerView.ViewHolder>(DiffCallback()) {

//        var previousCheckedPosition: Int? = null

    private var mNowCheckedPos:Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position)

                setSingleChecked(holder.binding.checkbox, position, data)

                holder.bind(data)
            }
        }
    }

    private fun setSingleChecked(checkbox: CheckBox, position: Int, data: GameData) {
        checkbox.setOnClickListener {
            val previousPosition = mNowCheckedPos

            if (previousPosition != null) {
//                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                getItem(previousPosition).isChecked = false
                notifyItemChanged(previousPosition)
            }

            mNowCheckedPos = position
//                checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
            checkbox.isChecked = true
            notifyItemChanged(position)
            checkedListener.onChecked(checkbox.isChecked, data)
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
/*

                itemView.apply {
                    checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            previousCheckedBtn = buttonView
                            data.isChecked = true
                        }
                    }
                }
*/

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

    class DiffCallback : DiffUtil.ItemCallback<GameData>() {
        override fun areItemsTheSame(oldItem: GameData, newItem: GameData): Boolean {
            return oldItem.showName == newItem.showName
        }

        override fun areContentsTheSame(oldItem: GameData, newItem: GameData): Boolean {
            return oldItem == newItem
        }

    }

}

