package org.cxct.sportlottery.ui.main.accountHistory.next

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
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

class ParlayItemAdapter : ListAdapter<ParlayDataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, NO_DATA
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var gameType: String = ""

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<MatchOdd>?, isLastPage: Boolean) {
        adapterScope.launch {
            val items = when {
                list.isNullOrEmpty() -> listOf(ParlayDataItem.NoData)
                isLastPage -> list.map { ParlayDataItem.Item(it) } + listOf(ParlayDataItem.Footer)
                else -> list.map { ParlayDataItem.Item(it) }
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
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as ParlayDataItem.Item
                holder.bind(data.matchOdd, oddsType, gameType)
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ParlayDataItem.Item -> ItemType.ITEM.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }


    class ItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentParlayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(matchOdd: MatchOdd, oddsType: OddsType, gameType: String) {

            binding.gameType = gameType
            binding.matchOdd = matchOdd

            matchOdd.let {
                val odds = getOdds(matchOdd, oddsType)
                val oddStr = if (odds > 0)
                    String.format(binding.root.context.getString(R.string.at_symbol, TextUtil.formatForOdd(odds)))
                else
                    ""
                binding.tvOdd.text = oddStr
            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentParlayItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }
    
    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false))
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<ParlayDataItem>() {
        override fun areItemsTheSame(oldItem: ParlayDataItem, newItem: ParlayDataItem): Boolean {
            return oldItem.matchId == newItem.matchId
        }

        override fun areContentsTheSame(oldItem: ParlayDataItem, newItem: ParlayDataItem): Boolean {
            return oldItem == newItem
        }

    }
}

sealed class ParlayDataItem {

    abstract val matchId: String?

    data class Item(val matchOdd: MatchOdd) : ParlayDataItem() {
        override val matchId = matchOdd.matchId
    }

    object Footer : ParlayDataItem() {
        override val matchId: String = ""
    }

    object NoData : ParlayDataItem() {
        override val matchId: String? = null
    }
}