package org.cxct.sportlottery.ui.profileCenter.sportRecord

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_bottom_sheet_sport_bet_record_item.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ContentBottomSheetSportBetRecordItemBinding

data class SheetData(val code: Int?, val showName: String?) {
    var isChecked = false
}

class BottomSheetAdapter (private val checkedListener: ItemCheckedListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                holder.bind(data, checkedListener)
            }
        }
    }

    class ItemViewHolder private constructor(val binding: ContentBottomSheetSportBetRecordItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SheetData, checkedListener: ItemCheckedListener) {

            itemView.apply {
                /*
                if (data.isChecked) {
                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
                } else {
                    checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                }
*/
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    data.isChecked = isChecked

                    if (data.isChecked) {
                        checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.blue2))
                    } else {
                        checkbox.setBackgroundColor(ContextCompat.getColor(checkbox.context, R.color.white))
                    }

                    checkedListener.onChecked()


                }

            }

            binding.item = data
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ContentBottomSheetSportBetRecordItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ItemCheckedListener(val checkedListener: () -> Unit) {
        fun onChecked() = checkedListener()
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

}