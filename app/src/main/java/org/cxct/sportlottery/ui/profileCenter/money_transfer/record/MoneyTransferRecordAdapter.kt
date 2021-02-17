package org.cxct.sportlottery.ui.profileCenter.money_transfer.record

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_recharge_log.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemMoneyTransferHistoryBinding
import org.cxct.sportlottery.network.third_game.query_transfers.Row

class MoneyTransferRecordAdapter (private val clickListener: ItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, NO_DATA, FOOTER
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<Row>?, isLastPage: Boolean) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.NoData)
                else -> {
                    when {
                        list.isEmpty() -> listOf(DataItem.NoData)
                        isLastPage -> {
                            list.map { DataItem.Item(it) } + listOf(DataItem.Footer)
                        }
                        else -> {
                            list.map { DataItem.Item(it) }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, clickListener)
            }

            is FooterViewHolder -> {}

            is NoDataViewHolder -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Item -> ItemType.ITEM.ordinal
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemMoneyTransferHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Row, clickListener: ItemClickListener) {
            binding.row = data
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMoneyTransferHistoryBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false)) //TODO Cheryl: UI??
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.orderNo == newItem.orderNo
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

    }
}

class ItemClickListener(val clickListener: (data: Row) -> Unit) {
    fun onClick(data: Row) = clickListener(data)
}

sealed class DataItem {

    abstract val orderNo: String?

    data class Item(val row: Row) : DataItem() {
        override val orderNo: String? = row.orderNo
    }
    object Footer : DataItem() {
        override val orderNo: String = ""
    }

    object NoData : DataItem() {
        override val orderNo: String? = null
    }
}