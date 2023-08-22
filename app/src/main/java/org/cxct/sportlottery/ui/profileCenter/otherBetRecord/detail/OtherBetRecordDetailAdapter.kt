package org.cxct.sportlottery.ui.profileCenter.otherBetRecord.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemOtherBetRecordDetailBinding
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.detail.OrderData

class OtherBetRecordDetailAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, NO_DATA
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<OrderData>?, isLastPage: Boolean) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.NoData)
                else -> {
                    when {
                        list.isEmpty() -> listOf(DataItem.NoData)
//                        isLastPage -> {
//                            list.map { DataItem.Item(it) } + listOf(DataItem.Footer)
//                        }
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
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val item = getItem(position) as DataItem.Item
            val isLastItem = position == itemCount -1
            holder.bind(item.data, isLastItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Item -> ItemType.ITEM.ordinal
//            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemOtherBetRecordDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: OrderData, isLastItem: Boolean) {
            binding.data = data
            binding.executePendingBindings()
            binding.divider.isVisible = !isLastItem
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemOtherBetRecordDetailBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_game_no_record, parent, false))
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

class ItemClickListener(val clickListener: (data: OrderData) -> Unit) {
    fun onClick(data: OrderData) = clickListener(data)
}

sealed class DataItem {

    abstract val orderNo: String?

    data class Item(val data: OrderData) : DataItem() {
        override val orderNo = data.orderNo
    }

//    object Footer : DataItem() {
//        override val orderNo: String? = null
//    }

    object NoData : DataItem() {
        override val orderNo: String? = null
    }
}