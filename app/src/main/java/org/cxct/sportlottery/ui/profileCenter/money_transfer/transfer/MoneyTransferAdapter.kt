package org.cxct.sportlottery.ui.profileCenter.money_transfer.transfer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemMoneyTransferBinding
import org.cxct.sportlottery.network.third_game.money_transfer.GameData
import org.cxct.sportlottery.ui.bet.record.search.result.BetDetailAdapter

class MoneyTransferAdapter (private val clickListener: ItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, NO_DATA
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<GameData>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.NoData)
                else -> {
                    if (list.isEmpty())
                        listOf(DataItem.NoData)
                    else
                        list.map { DataItem.Item(it) }
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
            ItemType.NO_DATA.ordinal -> NoDataViewHolder.from(parent)
            else -> ItemViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.gameData, clickListener)
                showDivider(position, holder)
            }

//            is FooterViewHolder -> {}

            is NoDataViewHolder -> {}
        }
    }

    private fun showDivider(position: Int, holder: ItemViewHolder) {
        if (position == itemCount - 1) holder.binding.divider.visibility = View.GONE
        else holder.binding.divider.visibility = View.VISIBLE
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Item -> ItemType.ITEM.ordinal
            is DataItem.NoData -> ItemType.NO_DATA.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemMoneyTransferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: GameData, clickListener: ItemClickListener) {
            binding.data = data
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemMoneyTransferBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }
/*

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                FooterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }
*/

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_third_game, parent, false)) //TODO Cheryl: UI??
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

    }
}

class ItemClickListener(val clickListener: (data: GameData) -> Unit) {
    fun onClick(data: GameData) = clickListener(data)
}

sealed class DataItem {

    abstract val name: String

    data class Item(val gameData: GameData) : DataItem() {
        override val name = gameData.name
    }
/*
    object Footer : DataItem() {
        override val name: String = ""
    }
*/

    object NoData : DataItem() {
        override val name: String = ""
    }
}