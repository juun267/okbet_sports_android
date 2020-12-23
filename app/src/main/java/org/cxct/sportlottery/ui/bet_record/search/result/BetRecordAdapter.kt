package org.cxct.sportlottery.ui.bet_record.search.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_bet_record_result.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.bet.list.Row

class BetRecordAdapter : ListAdapter<Row, RecyclerView.ViewHolder>(DiffCallback()) {

    var dataList = listOf<Row>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    enum class ItemType {
        ITEM, FOOTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            else -> {
                FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
            }
        }
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bet_record_result, parent, false)
//        Log.e(">>>", "onCreateViewHolder")
//        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(data)
            }

            is FooterViewHolder -> {
                holder.apply {
                }
            }
        }
    }


    override fun getItemCount() = dataList.size

    override fun getItemViewType(position: Int): Int {
        /*
        return when (position) {
            dataList.size -> ItemType.FOOTER.ordinal
            else -> ItemType.ITEM.ordinal
        }
        */
        return ItemType.ITEM.ordinal
    }

    class ItemViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup) =
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bet_record_result, parent, false))
        }

        fun bind(data: Row) {
            itemView.apply {
                tv_order_number.text = data.orderNo
            }
        }
    }

    class FooterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvNoData: TextView = view.findViewById(R.id.tv_no_data)
    }

}

class DiffCallback : DiffUtil.ItemCallback<Row>() {
    override fun areItemsTheSame(oldItem: Row, newItem: Row): Boolean {
        return oldItem.orderNo == newItem.orderNo
    }

    override fun areContentsTheSame(oldItem: Row, newItem: Row): Boolean {
        return oldItem == newItem
    }

}
