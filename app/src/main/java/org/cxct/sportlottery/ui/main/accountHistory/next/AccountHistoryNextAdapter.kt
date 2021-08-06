package org.cxct.sportlottery.ui.main.accountHistory.next

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_account_history_next_title_bar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.*
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.Other
import org.cxct.sportlottery.network.bet.settledDetailList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.TextUtil
import org.cxct.sportlottery.util.TimeUtil
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT
import org.cxct.sportlottery.util.getOdds

class AccountHistoryNextAdapter(private val itemClickListener: ItemClickListener,
                                private val backClickListener: BackClickListener,
                                private val sportDateSelectListener: SportDateSelectListener
                                ) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        TITLE_BAR, ITEM, PARLAY, OUTRIGHT, FOOTER, NO_DATA
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var mOther: Other? = null

    var mSelectedSportDate : Pair<String?, String?>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(other: Other?, list: List<Row>?, isLastPage: Boolean) {
        mOther = other
        adapterScope.launch {
            val items = listOf(DataItem.TitleBar) + when {
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
            ItemType.TITLE_BAR.ordinal -> TitleBarViewHolder.from(parent)
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.OUTRIGHT.ordinal -> OutrightItemViewHolder.from(parent)
            ItemType.PARLAY.ordinal -> ParlayItemViewHolder.from(parent)
            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is TitleBarViewHolder -> {
                holder.bind(mSelectedSportDate, backClickListener, sportDateSelectListener)
            }

            is ItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType)
            }

            is OutrightItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType)
            }

            is ParlayItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType)
            }

            is FooterViewHolder -> {
                holder.bind(mOther)
            }

            is NoDataViewHolder -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TitleBar -> ItemType.TITLE_BAR.ordinal
            is DataItem.Item -> {
                when (getItem(position).parlayType) {
                    "1C1" -> ItemType.ITEM.ordinal
                    "OUTRIGHT" -> ItemType.OUTRIGHT.ordinal
                    else -> ItemType.PARLAY.ordinal
                }
            }
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }


    class ParlayItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentParlayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val parlayAdapter by lazy { ParlayItemAdapter() }

        fun bind(row: Row, oddsType: OddsType) {
            binding.matchOdd = row.matchOdds?.firstOrNull()
            binding.row = row
            binding.tvParlayType.text = row.parlayType?.replace("C", "串")

            binding.rvParlay.apply {
                adapter = parlayAdapter
                layoutManager = LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                parlayAdapter.addFooterAndSubmitList(row.matchOdds, false) //TODO Cheryl: 是否需要換頁
                parlayAdapter.oddsType = oddsType
                parlayAdapter.gameType = row.gameType ?: ""
            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentParlayBinding.inflate(layoutInflater, parent, false)
                return ParlayItemViewHolder(binding)
            }
        }

    }


    class OutrightItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentOutrightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row, oddsType: OddsType) {
            val first = row.matchOdds?.firstOrNull()

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
                val binding = ItemAccountHistoryNextContentOutrightBinding.inflate(layoutInflater, parent, false)
                return OutrightItemViewHolder(binding)
            }
        }

    }


    class ItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row, oddsType: OddsType) {
            val first = row.matchOdds?.firstOrNull()

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
        fun bind(data: Other?) {
            binding.other = data
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

    class TitleBarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(mSelectedSportDate: Pair<String?, String?>?, backClickListener: BackClickListener, selectListener: SportDateSelectListener) {
            itemView.apply {

                iv_back.setOnClickListener {
                    backClickListener.onClick()
                }
                
                val sportStatusList =
                    listOf(
                        StatusSheetData("", context?.getString(R.string.all_sport)),
                        StatusSheetData(GameType.FT.key, context?.getString(GameType.FT.string)),
                        StatusSheetData(GameType.BK.key, context?.getString(GameType.BK.string)),
                        StatusSheetData(GameType.TN.key, context?.getString(GameType.TN.string)),
                        StatusSheetData(GameType.VB.key, context?.getString(GameType.VB.string))
                    )

                val dateString = { minusDate: Int ->
                    "${TimeUtil.getMinusDate(minusDate)} ${context.getString(TimeUtil.getMinusDayOfWeek(minusDate))}"
                }

                val dateStatusList =
                    listOf(StatusSheetData(TimeUtil.getMinusDate(0, YMD_FORMAT), dateString(0)),
                           StatusSheetData(TimeUtil.getMinusDate(1, YMD_FORMAT), dateString(1)),
                           StatusSheetData(TimeUtil.getMinusDate(2, YMD_FORMAT), dateString(2)),
                           StatusSheetData(TimeUtil.getMinusDate(3, YMD_FORMAT), dateString(3)),
                           StatusSheetData(TimeUtil.getMinusDate(4, YMD_FORMAT), dateString(4)),
                           StatusSheetData(TimeUtil.getMinusDate(5, YMD_FORMAT), dateString(5)),
                           StatusSheetData(TimeUtil.getMinusDate(6, YMD_FORMAT), dateString(6)),
                           StatusSheetData(TimeUtil.getMinusDate(7, YMD_FORMAT), dateString(7)))


                sport_selector.selectedText = sportStatusList.find { it.code == mSelectedSportDate?.first }?.showName
                sport_selector.selectedTag = mSelectedSportDate?.first

                date_selector.selectedText = dateStatusList.find { it.code == mSelectedSportDate?.second }?.showName
                date_selector.selectedTag = mSelectedSportDate?.second

                sport_selector.setCloseBtnText(context.getString(R.string.bottom_sheet_close))
                sport_selector.dataList = sportStatusList
                sport_selector.setOnItemSelectedListener {
                    selectListener.onSelect(it.code, date_selector.getNowSelectedItemCode())
                }

                date_selector.setCloseBtnText(context.getString(R.string.bottom_sheet_close))
                date_selector.dataList = dateStatusList
                date_selector.setOnItemSelectedListener {
                    selectListener.onSelect(sport_selector.getNowSelectedItemCode(), it.code)
                }

            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                TitleBarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_account_history_next_title_bar, parent, false))
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

class BackClickListener(val clickListener: () -> Unit) {
    fun onClick() = clickListener()
}

class SportDateSelectListener(val selectedListener: (sport: String?, date: String?) -> Unit) {
    fun onSelect(sport: String?, date: String?) = selectedListener(sport, date)
}

sealed class DataItem {

    abstract val orderNum: String?
    abstract val parlayType: String?

    data class Item(val row: Row) : DataItem() {
        override val orderNum = row.orderNo
        override val parlayType = row.parlayType
    }

    object TitleBar : DataItem() {
        override val orderNum: String = ""
        override val parlayType = ""
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