package org.cxct.sportlottery.ui.main.accountHistory.next

import android.graphics.Paint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.content_parlay_record.view.*
import kotlinx.android.synthetic.main.view_account_history_next_title_bar.view.*
import kotlinx.android.synthetic.main.view_back_to_top.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cxct.sportlottery.MultiLanguagesApplication
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentOutrightBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentParlayBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextTotalBinding
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.Other
import org.cxct.sportlottery.network.bet.settledDetailList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TextUtil.getParlayShowName
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT

class AccountHistoryNextAdapter(
    private val itemClickListener: ItemClickListener,
    private val backClickListener: BackClickListener,
    private val sportSelectListener: SportSelectListener,
    private val dateSelectListener: DateSelectListener,
    private val scrollToTopListener: ScrollToTopListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
        TITLE_BAR, ITEM, PARLAY, OUTRIGHT, FOOTER, BACK_TO_TOP, NO_DATA
    }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var mOther: Other? = null

    var nowSelectedDate: String? = ""
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    var nowSelectedSport: String? = ""
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addFooterAndSubmitList(other: Other?, list: List<Row>?, isLastPage: Boolean) {
        mOther = other
        adapterScope.launch {
            val items = listOf(DataItem.TitleBar) + when {
                list.isNullOrEmpty() -> listOf(DataItem.NoData)
                isLastPage -> list.map { DataItem.Item(it) } + listOf(DataItem.Footer) + listOf(DataItem.BackToTop)
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
            ItemType.BACK_TO_TOP.ordinal -> BackToTopViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

            is TitleBarViewHolder -> {
                holder.bind(
                    nowSelectedDate,
                    nowSelectedSport,
                    backClickListener,
                    sportSelectListener,
                    dateSelectListener
                )
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

            is BackToTopViewHolder -> {
                holder.bind(scrollToTopListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.TitleBar -> ItemType.TITLE_BAR.ordinal
            is DataItem.Item -> {
                when (getItem(position).parlayType) {
                    ParlayType.SINGLE.key -> ItemType.ITEM.ordinal
                    ParlayType.OUTRIGHT.key -> ItemType.OUTRIGHT.ordinal
                    else -> ItemType.PARLAY.ordinal
                }
            }
            is DataItem.Footer -> ItemType.FOOTER.ordinal
            is DataItem.BackToTop -> ItemType.BACK_TO_TOP.ordinal
            else -> ItemType.NO_DATA.ordinal
        }
    }


    class ParlayItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentParlayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: Row, oddsType: OddsType) {
            val parlayAdapter by lazy { ParlayItemAdapter() }

            binding.row = row

            binding.apply {
                matchOdd = row.matchOdds?.firstOrNull()

                row.parlayType?.let { parlayType ->
                    ParlayType.getParlayStringRes(parlayType)?.let { parlayTypeStringResId ->
                        tvParlayType.text = MultiLanguagesApplication.appContext.getString(parlayTypeStringResId)
                    }
                }

                tvDetail.paint.flags = Paint.UNDERLINE_TEXT_FLAG
                tvDetail.isVisible = (row.parlayComsDetailVOs ?: emptyList()).isNotEmpty()
                tvDetail.setOnClickListener {
                    val dialog = row.parlayComsDetailVOs?.let { list ->
                        ComboDetailDialog(it.context, list)
                    }
                    dialog?.show()
                }
                rvParlay.apply {
                    adapter = parlayAdapter
                    layoutManager =
                        LinearLayoutManager(itemView.context, RecyclerView.VERTICAL, false)
                    parlayAdapter.addFooterAndSubmitList(row.matchOdds, false) //TODO Cheryl: 是否需要換頁
                    parlayAdapter.oddsType = oddsType
                    parlayAdapter.gameType = row.gameType ?: ""
                }
                executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
            }
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentParlayBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
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

            first?.apply {
                first.oddsType?.let {
                    binding.playContent.setPlayContent(
                        playName,
                        spread,
                        odds?.let { TextUtil.formatForOdd(it) }
                    )
                }
                binding.tvGameTypePlayCate.text = "${GameType.getGameTypeString(binding.tvGameTypePlayCate.context, row.gameType)} $playCateName"

                if (!homeName.isNullOrEmpty() && !awayName.isNullOrEmpty()) {
                    binding.tvTeamNames.setTeamNames(15, homeName, awayName)
                    binding.tvTeamNames.visibility = View.VISIBLE
                } else {
                    binding.tvTeamNames.visibility = View.GONE
                }

                startTime?.let {
                    binding.tvStartTime.text = TimeUtil.timeFormat(it, TimeUtil.YMD_HM_FORMAT)
                }
                binding.tvStartTime.isVisible = row.parlayType != ParlayType.OUTRIGHT.key
            }
            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentOutrightBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return OutrightItemViewHolder(binding)
            }
        }

    }

    class ItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: Row, oddsType: OddsType) {
            val first = row.matchOdds?.firstOrNull()

            val roundAdapter by lazy { RoundAdapter() }

            binding.row = row
            binding.matchOdd = first

            first?.let { it ->

                binding.tvContent.setPlayContent(
                    it.playName,
                    it.spread,
                    it.odds?.let { odd -> TextUtil.formatForOdd(odd) }
                )

                binding.tvStartTime.text = TimeUtil.timeFormat(it.startTime, TimeUtil.YMD_HM_FORMAT)

                val scoreList = mutableListOf<String>()
                it.playCateMatchResultList?.map { scoreData ->
                    scoreList.add(
                        "${
                            scoreData.statusNameI18n?.get(
                                LanguageManager.getSelectLanguage(
                                    itemView.context
                                ).key
                            )
                        }: ${scoreData.score}"
                    )
                }

                when (row.gameType) {
                    GameType.FT.key -> {
                        if (it.rtScore?.isNotEmpty() == true)
                            binding.tvScore.text = String.format(
                                binding.tvScore.context.getString(R.string.brackets),
                                it.rtScore
                            )
                    }
                }
                binding.listScore.apply {
                    layoutManager =
                        LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
                    adapter = roundAdapter
                }
                roundAdapter.submitList(scoreList)

            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ItemAccountHistoryNextContentBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class FooterViewHolder private constructor(val binding: ItemAccountHistoryNextTotalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Other?) {
            binding.other = data
            binding.executePendingBindings()
            binding.tvCurrencyType.text = sConfigData?.systemCurrency
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ItemAccountHistoryNextTotalBinding.inflate(layoutInflater, parent, false)
                return FooterViewHolder(binding)
            }
        }

    }

    class TitleBarViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val sportStatusList = mutableListOf<StatusSheetData>().apply {
            this.add(StatusSheetData("", itemView.context.getString(R.string.all_sport)))

            val itemList = GameType.values()
            itemList.forEach { gameType ->
                this.add(StatusSheetData(gameType.key, GameType.getGameTypeString(itemView.context, gameType.key)))
            }
        }

        private val dateString by lazy {
            { minusDate: Int ->
                "${TimeUtil.getMinusDate(minusDate)} ${
                    itemView.context.getString(
                        TimeUtil.getMinusDayOfWeek(
                            minusDate
                        )
                    )
                }"
            }
        }

        private val dateStatusList = mutableListOf<StatusSheetData>().apply {
            for (i in 0..7) {
                this.add(StatusSheetData(TimeUtil.getMinusDate(i, YMD_FORMAT), dateString(i)))
            }
        }

        fun bind(
            nowSelectedDate: String?,
            nowSelectedSport: String?,
            backClickListener: BackClickListener,
            sportSelectListener: SportSelectListener,
            dateSelectListener: DateSelectListener
        ) {
            itemView.apply {

                iv_back.setOnClickListener {
                    backClickListener.onClick()
                }

                tv_title.setTextWithStrokeWidth(context?.getString(R.string.bet_num_and_bet_date) ?: "", 0.7f)

                date_selector.cl_root.layoutParams.height = 40.dp
                sport_selector.cl_root.layoutParams.height = 40.dp

                //sport
                sport_selector.setCloseBtnText(context.getString(R.string.bottom_sheet_close))
                sport_selector.dataList = sportStatusList

                sport_selector.selectedText =
                    sportStatusList.find { it.code == nowSelectedSport }?.showName
                sport_selector.selectedTag = nowSelectedSport

                sport_selector.setOnItemSelectedListener {
                    sportSelectListener.onSelect(it.code)
                }

                //date
                date_selector.setCloseBtnText(context.getString(R.string.bottom_sheet_close))
                date_selector.dataList = dateStatusList

                date_selector.selectedText =
                    dateStatusList.find { it.code == nowSelectedDate }?.showName
                date_selector.selectedTag = nowSelectedDate

                date_selector.setOnItemSelectedListener {
                    dateSelectListener.onSelect(it.code)
                }

            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                TitleBarViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_account_history_next_title_bar, parent, false)
                )
        }
    }

    class BackToTopViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(scrollToTopListener: ScrollToTopListener) {
            itemView.btn_back_to_top.setOnClickListener {
                scrollToTopListener.onClick()
            }
        }

        companion object {
            fun from(parent: ViewGroup) =
                BackToTopViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.view_back_to_top, parent, false)
                )
        }
    }

    class NoDataViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup) =
                NoDataViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.itemview_game_no_record, parent, false)
                )
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

class DateSelectListener(val selectedListener: (date: String?) -> Unit) {
    fun onSelect(date: String?) = selectedListener(date)
}

class SportSelectListener(val selectedListener: (sport: String?) -> Unit) {
    fun onSelect(sport: String?) = selectedListener(sport)
}

class ScrollToTopListener(val clickListener: () -> Unit) {
    fun onClick() = clickListener()
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

    object BackToTop : DataItem() {
        override val orderNum: String? = null
        override val parlayType = ""
    }
}