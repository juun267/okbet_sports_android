package org.cxct.sportlottery.ui.main.accountHistory.next

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextTotalBinding
import org.cxct.sportlottery.databinding.ItemBetRecordDetailBinding
import org.cxct.sportlottery.network.bet.MatchOdd
import org.cxct.sportlottery.network.bet.list.Row
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.getOdds

class AccountHistoryNextAdapter(private val clickListener: ItemClickListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        ITEM, PARLAY, OUTRIGHT, FOOTER, NO_DATA
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: List<Row>?, isLastPage: Boolean) {
        adapterScope.launch {
            val items = when {
                list.isNullOrEmpty() -> listOf(DataItem.NoData)
                isLastPage -> list.map { DataItem.Item(it) } + listOf(DataItem.Footer)

                else -> list.map { DataItem.Item(it) }
            }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.OUTRIGHT.ordinal -> OutrightItemViewHolder.from(parent)
            ItemType.PARLAY.ordinal -> ParlayItemViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType)
            }

            is OutrightItemViewHolder -> {

            }

            is ParlayItemViewHolder -> {

            }

            is FooterViewHolder -> {
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        return when (getItem(position).parlayType) {
            "1C1" -> ItemType.PARLAY.ordinal
            "OUTRIGHT" -> ItemType.OUTRIGHT.ordinal
            else -> ItemType.ITEM.ordinal
        }

        /*
        return when (getItem(position)) {
            is DataItem.Item -> ItemType.ITEM.ordinal
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
        */
    }

    class ItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row, oddsType: OddsType) {
            val first = row.matchOdds.firstOrNull()

            binding.row = row
            binding.matchOdd = first

            first?.let {
                val odds = getOdds(first, oddsType)
                val oddStr = if (odds > 0)
                    String.format(binding.root.context.getString(R.string.at_symbol, TextUtil.formatForOdd(odds)))
                else
                    ""
                binding.tvOdd.text = oddStr
                binding.tvOddType.apply {
                    text = this.context.getString(oddsType.res)
                }
            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class FooterViewHolder private constructor(val binding: ItemAccountHistoryNextTotalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Row) {
            binding.row = data
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextTotalBinding.inflate(layoutInflater, parent, false)
                return FooterViewHolder(binding)
            }
        }

    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false))
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
}

class ItemClickListener(val clickListener: (data: MatchOdd) -> Unit) {
    fun onClick(data: MatchOdd) = clickListener(data)
}

sealed class DataItem {

    abstract val orderNum: String?
    abstract val parlayType: String?

    data class Item(val row: Row) : DataItem() {
        override val orderNum = row.orderNo
        override val parlayType = row.parlayType
    }

    object Footer : DataItem() {
        override val orderNum: String = ""
        override val parlayType = ""
    }

    object NoData : DataItem() {
        override val orderNum: String? = null
        override val parlayType = ""
    }
}