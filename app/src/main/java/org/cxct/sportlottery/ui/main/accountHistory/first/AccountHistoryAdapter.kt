package org.cxct.sportlottery.ui.main.accountHistory.first

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_account_history_title_bar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryBinding
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil

class AccountHistoryAdapter(private val clickListener: ItemClickListener,
                            private val backClickListener: BackClickListener,
                            private val sportSelectListener: SportSelectListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        TITLE_BAR, ITEM, FOOTER, NO_DATA
    }


    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var allDateList = mutableListOf<Row>().apply {
        for (i in 0 until 8) {
            add(Row(statDate = TimeUtil.getMinusDate(i, TimeUtil.YMD_FORMAT)))
        }
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: MutableList<Row?>, isLastPage: Boolean) {
        adapterScope.launch {

            val newList = mappingDateList(list)

            val items = listOf(DataItem.TitleBar) + when {
                newList.isNullOrEmpty() -> listOf(DataItem.NoData)
                isLastPage -> newList.map { DataItem.Item(it) } + listOf(DataItem.Footer)
                else -> newList.map { DataItem.Item(it) }
            }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
    }

    private fun mappingDateList(dataList: MutableList<Row?>): List<Row> {

        allDateList.apply {
            for (i in 0 until 8) {
                allDateList[i] = (Row(statDate = TimeUtil.getMinusDate(i, TimeUtil.YMD_FORMAT)))
            }
        }

        allDateList.forEachIndexed { index, allDate ->
            dataList.forEach { data ->
                if (allDate.statDate == data?.statDate) {
                    if (data != null) {
                        allDateList[index] = data
                    }
                }
            }
        }

        return allDateList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.TITLE_BAR.ordinal -> TitleBarViewHolder.from(parent)
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleBarViewHolder -> {
                holder.bind(backClickListener, sportSelectListener)
            }

            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, clickListener)
            }

            is FooterViewHolder -> {
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TitleBar -> ItemType.TITLE_BAR.ordinal
            is DataItem.Item -> ItemType.ITEM.ordinal
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }

    class ItemViewHolder private constructor(val binding: ItemAccountHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Row, clickListener: ItemClickListener) {
            binding.row = data
            binding.textUtil = TextUtil

            itemView.setOnClickListener {
                clickListener.onClick(data)
            }

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class TitleBarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(backClickListener: BackClickListener, sportSelectListener: SportSelectListener) {
            itemView.apply {

                val sportStatusList = listOf(
                    StatusSheetData("", context?.getString(R.string.all_sport)),
                    StatusSheetData(GameType.FT.key, context?.getString(GameType.FT.string)),
                    StatusSheetData(GameType.BK.key, context?.getString(GameType.BK.string)),
                    StatusSheetData(GameType.TN.key, context?.getString(GameType.TN.string)),
                    StatusSheetData(GameType.VB.key, context?.getString(GameType.VB.string)),
                )

                iv_back.setOnClickListener {
                    backClickListener.onClick()
                }

                status_selector.setCloseBtnText(context?.getString(R.string.bottom_sheet_close))
                status_selector.dataList = sportStatusList
                status_selector.setOnItemSelectedListener {
                    sportSelectListener.onSelect(it.code)
                }

            }
        }

        companion object {
            fun from(parent: ViewGroup) = TitleBarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_account_history_title_bar, parent, false))
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_no_record, parent, false))
        }
    }

    class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) = NoDataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_footer_no_data, parent, false))
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<DataItem>() {
        override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
            return oldItem == newItem
        }
    }
}

class ItemClickListener(val clickListener: (data: Row) -> Unit) {
    fun onClick(data: Row) = clickListener(data)
}

class SportSelectListener(val selectedListener: (sport: String?) -> Unit) {
    fun onSelect(sport: String?) = selectedListener(sport)
}

class BackClickListener(val clickListener: () -> Unit) {
    fun onClick() = clickListener()
}


sealed class DataItem {

    abstract val rowItem: Row?

    data class Item(val row: Row) : DataItem() {
        override val rowItem = row
    }

    object TitleBar : DataItem() {
        override val rowItem: Row? = null
    }

    object Footer : DataItem() {
        override val rowItem: Row? = null
    }

    object NoData : DataItem() {
        override val rowItem: Row? = null
    }
}