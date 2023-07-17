package org.cxct.sportlottery.ui.profileCenter.otherBetRecord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemOtherBetRecordBinding
import org.cxct.sportlottery.network.third_game.third_games.other_bet_history.Order

class OtherBetRecordAdapter(private val clickListener: ItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
//        ITEM, FOOTER, NO_DATA
        ITEM, NO_DATA
    }


    fun addFooterAndSubmitList(list: List<Order>?, isLastPage: Boolean) {

        val items: List<DataItem> = when (list) {
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

        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
//            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = getItem(position) as DataItem.Item
                val isLastItem = position == itemCount -1
                holder.bind(item.data, clickListener, isLastItem)
            }

//            is FooterViewHolder -> {}

            is NoDataViewHolder -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Item -> ItemType.ITEM.ordinal
//            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemOtherBetRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Order, clickListener: ItemClickListener, isLastItem: Boolean) {
            binding.data = data
            binding.clickListener = clickListener
            binding.executePendingBindings()
            binding.divider.isVisible = !isLastItem
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemOtherBetRecordBinding.inflate(layoutInflater, parent, false)
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
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_game_no_record, parent, false))
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.statDate == newItem.statDate
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

    }
}

class ItemClickListener(val clickListener: (data: Order) -> Unit) {
    fun onClick(data: Order) = clickListener(data)
}

sealed class DataItem {

    abstract val statDate: Long?

    data class Item(val data: Order) : DataItem() {
        override val statDate = data.statDate
    }

//    object Footer : DataItem() {
//        override val statDate: Long? = null
//    }

    object NoData : DataItem() {
        override val statDate: Long? = null
    }
}