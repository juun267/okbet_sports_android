package org.cxct.sportlottery.ui.main.accountHistory.first

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_account_history_title_bar.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryBinding
import org.cxct.sportlottery.network.bet.settledList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil

class AccountHistoryAdapter(private val clickListener: ItemClickListener,
                            private val backClickListener: BackClickListener,
                            private val sportSelectListener: SportSelectListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        TITLE_BAR, ITEM, FOOTER, NO_DATA
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(list: MutableList<Row?>, isLastPage: Boolean) {
        adapterScope.launch {
            val reverseList = list.reversed()
            val items = listOf(DataItem.TitleBar) + when {
                reverseList.isNullOrEmpty() -> listOf(DataItem.NoData)
                isLastPage -> reverseList.map { DataItem.Item(it) } + listOf(DataItem.Footer)
                else -> reverseList.map { DataItem.Item(it) }
            }

            withContext(Dispatchers.Main) { //update in main ui thread
                submitList(items)
            }
        }
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
        fun bind(data: Row?, clickListener: ItemClickListener) {
            binding.row = data
            binding.textUtil = TextUtil

            itemView.setOnClickListener {
                if (data != null) {
                    clickListener.onClick(data)
                }
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
                val sportStatusList = mutableListOf<StatusSheetData>().apply {

                    this.add(StatusSheetData("", context?.getString(R.string.all_sport)))

                    val itemList = GameType.values()
                    itemList.forEach { gameType ->
                        this.add(StatusSheetData(gameType.key, GameType.getGameTypeString(context, gameType.key)))
                    }
                }

                iv_back.setOnClickListener {
                    backClickListener.onClick()
                }

                sportSelectListener.onSelect("")
                status_selector.cl_root.layoutParams.height = 40.dp
                status_selector.tv_selected.gravity = Gravity.CENTER_VERTICAL or Gravity.START
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

    data class Item(val row: Row?) : DataItem() {
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