package org.cxct.sportlottery.ui.bet_record.search.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemBetRecordResultBinding
import org.cxct.sportlottery.network.bet.list.Row

class BetRecordAdapter(private val clickListener: ItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, FOOTER
    }
    private val adaoterScrope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<Row>?) {
        adaoterScrope.launch {
            val items = when(list) {
                null -> listOf(DataItem.Footer)
                else -> list.map { DataItem.Item(it)} + listOf(DataItem.Footer)
            }
            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            else -> {
                FooterViewHolder.from(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, clickListener)
            }

            is FooterViewHolder -> {
            }
        }
    }



    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Item  -> ItemType.ITEM.ordinal
            else -> ItemType.FOOTER.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemBetRecordResultBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Row, clickListener: ItemClickListener) {
            binding.row = data
            binding.clickListener = clickListener
            binding.executePendingBindings()
//            binding.tvOrderNumber.text = data.orderNo
            /*
            itemView.apply {
                tv_order_number.text = data.orderNo
            }
            */
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
//                val view = layoutInflater.inflate(R.layout.item_bet_record_result, parent, false)
                val binding = ItemBetRecordResultBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class FooterViewHolder(view: View): RecyclerView.ViewHolder(view) {
//        val tvNoData: TextView = view.findViewById(R.id.tv_no_data)
        companion object {
             fun from(parent: ViewGroup) =
                FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }


}

class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.orderNum == newItem.orderNum
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }

}

class ItemClickListener(val clickListener: (orderNum: String) -> Unit) {
    fun onClick(data: Row) = clickListener(data.orderNo)
}

sealed class DataItem {

    abstract val orderNum: String

    data class Item(val row: Row): DataItem() {
        override val orderNum = row.orderNo
    }

    object Footer: DataItem() {
        override val orderNum: String = ""
    }
}