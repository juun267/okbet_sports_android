package org.cxct.sportlottery.ui.main.accountHistory.next

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_account_history_next_title_bar.view.*
import kotlinx.android.synthetic.main.view_back_to_top.view.*
import kotlinx.android.synthetic.main.view_status_selector.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentOutrightBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentParlayBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextTotalBinding
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.Other
import org.cxct.sportlottery.network.bet.settledDetailList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.common.StatusSheetData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.transactionStatus.ParlayType
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.TimeUtil.YMD_FORMAT

class AccountHistoryNextAdapter(
    private val itemClickListener: ItemClickListener,
    private val backClickListener: BackClickListener,
    private val sportSelectListener: SportSelectListener,
    private val dateSelectListener: DateSelectListener,
    private val scrollToTopListener: ScrollToTopListener
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(DiffCallback()) {

    enum class ItemType {
//        TITLE_BAR, ITEM, PARLAY, OUTRIGHT, FOOTER, BACK_TO_TOP, NO_DATA
        ITEM, PARLAY, OUTRIGHT, NO_DATA
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

    private var mRowList: List<Row>? = listOf() //資料清單
    private var mIsLastPage: Boolean = false //是否最後一頁資料
    private var mSportTypeList: List<StatusSheetData> = listOf() //球種篩選清單

    /**
     * 配置當前可用球種代號
     */
    fun setSportCodeSpinner(sportCodeList: List<StatusSheetData>) {
        mSportTypeList = sportCodeList

        updateData()
    }

    fun addFooterAndSubmitList(other: Other?, list: List<Row>?, isLastPage: Boolean) {
        mOther = other
        mRowList = list
        mIsLastPage = isLastPage

        updateData()
    }

    /**
     * 更新 球種篩選清單、資料清單
     *
     * 根據原邏輯提取為function
     */
    private fun updateData() {

//        val items = listOf(DataItem.TitleBar(mSportTypeList)) + when {
        val items = when {
            mRowList.isNullOrEmpty() -> listOf(DataItem.NoData)
            mIsLastPage -> mutableListOf<DataItem>().apply {
                mRowList?.map { DataItem.Item(it) }?.forEach {
                    add(it)
                }
//                add(DataItem.Footer)
//                add(DataItem.BackToTop)
            }
            else -> mutableListOf<DataItem>().apply {
                mRowList?.map { DataItem.Item(it) }?.forEach {
                    add(it)
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            submitList(items)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
//            ItemType.TITLE_BAR.ordinal -> TitleBarViewHolder.from(parent)
            ItemType.ITEM.ordinal -> ItemViewHolder.from(parent)
            ItemType.OUTRIGHT.ordinal -> OutrightItemViewHolder.from(parent)
            ItemType.PARLAY.ordinal -> ParlayItemViewHolder.from(parent)
//            ItemType.FOOTER.ordinal -> FooterViewHolder.from(parent)
//            ItemType.BACK_TO_TOP.ordinal -> BackToTopViewHolder.from(parent)
            else -> NoDataViewHolder.from(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {

//            is TitleBarViewHolder -> {
//                when (val data = getItem(position)) {
//                    is DataItem.TitleBar -> {
//                        holder.bind(
//                            data.spinnerList,
//                            nowSelectedDate,
//                            nowSelectedSport,
//                            backClickListener,
//                            sportSelectListener,
//                            dateSelectListener
//                        )
//                    }
//                    else -> {
//                        Timber.e("data of TitleBar no match")
//                    }
//                }
//            }

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

//            is FooterViewHolder -> {
//                holder.bind(mOther)
//            }

            is NoDataViewHolder -> {
            }

//            is BackToTopViewHolder -> {
//                holder.bind(scrollToTopListener)
//            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
//            is DataItem.TitleBar -> ItemType.TITLE_BAR.ordinal
            is DataItem.Item -> {
                when (getItem(position).parlayType) {
                    ParlayType.SINGLE.key -> ItemType.ITEM.ordinal
                    ParlayType.OUTRIGHT.key -> ItemType.OUTRIGHT.ordinal
                    else -> ItemType.PARLAY.ordinal
                }
            }
//            is DataItem.Footer -> ItemType.FOOTER.ordinal
//            is DataItem.BackToTop -> ItemType.BACK_TO_TOP.ordinal
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
                        tvParlayType.text = LocalUtils.getString(parlayTypeStringResId)
                    }
                }

//                tvDetail.paint.flags = Paint.UNDERLINE_TEXT_FLAG
//                tvDetail.isVisible = (row.parlayComsDetailVOs ?: emptyList()).isNotEmpty()
//                tvDetail.setOnClickListener {
//                    val dialog = row.parlayComsDetailVOs?.let { list ->
//                        ComboDetailDialog(it.context, list)
//                    }
//                    dialog?.show()
//                }
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

    class OutrightItemViewHolder private constructor(val binding: ItemAccountHistoryNextContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(row: Row, oddsType: OddsType) {
            val first = row.matchOdds?.firstOrNull()

            binding.row = row
            binding.matchOdd = first

            first?.apply {
                first.oddsType?.let {
                    val formatForOdd = if (playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage((odds ?: 0.0) - 1.0) else TextUtil.formatForOdd(odds ?: 0)
                    binding.tvContent.setPlayContent(
                        playName,
                        spread,
                        formatForOdd
                    )
                }

                val gameType = row.gameType.orEmpty()

                val singleTitle = itemView.context.getString(R.string.bet_record_single) +
                        "-${getGameTypeName(gameType)}"
                binding.tvTitle.text = singleTitle

                val oddsTypeStr = when (this.oddsType) {
                    OddsType.HK.code -> "【" + itemView.context.getString(OddsType.HK.res) + "】"
                    OddsType.MYS.code -> "【" + itemView.context.getString(OddsType.MYS.res) + "】"
                    OddsType.IDN.code -> "【" + itemView.context.getString(OddsType.IDN.res) + "】"
                    else -> "【" + itemView.context.getString(OddsType.EU.res) + "】"
                }
                binding.tvGameTypePlayCate.text = if (row.matchType != null) {
                    //篮球 滚球 全场让分【欧洲盘】
                    "${getGameTypeName(gameType)} ${getMatchTypeName(row.matchType)} ${this.playCateName}$oddsTypeStr"
                } else {
                    "${getGameTypeName(gameType)} ${this.playCateName}$oddsTypeStr"
                }

                if (!homeName.isNullOrEmpty() && !awayName.isNullOrEmpty()) {
                    binding.tvTeamNamesSingles.setTeamsNameWithVS(homeName, awayName)
                } else {
                    binding.tvTeamNamesSingles.text = this.leagueName
                }

                startTime?.let {
                    binding.tvStartTime.text = TimeUtil.timeFormat(it, TimeUtil.DM_HM_FORMAT)
                }
                binding.tvStartTime.isVisible = row.parlayType != ParlayType.OUTRIGHT.key

                binding.llCopyBetOrder.setOnClickListener {
                    itemView.context.copyToClipboard(row.orderNo.orEmpty())
                }
            }
            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

        private fun getMatchTypeName(matchType: String?): String {
            return itemView.context.getString(MatchType.getMatchTypeStringRes(matchType))
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemAccountHistoryNextContentBinding.inflate(
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

                val formatForOdd = if (it.playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage((it.odds ?: 0.0) - 1.0) else TextUtil.formatForOdd(it.odds ?: 0)
                binding.tvContent.setPlayContent(
                    it.playName,
                    it.spread,
                    formatForOdd
                )

                binding.tvStartTime.text = TimeUtil.timeFormat(it.startTime, TimeUtil.DM_HM_FORMAT)

//                val scoreList = mutableListOf<String>()
//                it.playCateMatchResultList?.map { scoreData ->
//                    scoreList.add(
//                        "${
//                            scoreData.statusNameI18n?.get(
//                                LanguageManager.getSelectLanguage(
//                                    itemView.context
//                                ).key
//                            )
//                        }: ${scoreData.score}"
//                    )
//                }
//
//                when (row.gameType) {
//                    GameType.FT.key -> {
//                        if (it.rtScore?.isNotEmpty() == true) {
//                            binding.tvScore.text = "(${it.rtScore})"
//
//                            binding.tvScore.visibility = View.VISIBLE
//                        }
//                        else {
//                            binding.tvScore.visibility = View.GONE
//                        }
//                    }
//                    else -> {
//                        binding.tvScore.visibility = View.GONE
//                    }
//                }
//                binding.listScore.apply {
//                    layoutManager =
//                        LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
//                    adapter = roundAdapter
//                }
//                roundAdapter.submitList(scoreList)

                val gameType = row.gameType.orEmpty()

                val singleTitle = itemView.context.getString(R.string.bet_record_single) +
                        "-${getGameTypeName(gameType)}"
                binding.tvTitle.text = singleTitle

                binding.tvTeamNamesSingles.setTeamsNameWithVS(it.homeName, it.awayName)

                val oddsTypeStr = when (it.oddsType) {
                    OddsType.HK.code -> "【" + itemView.context.getString(OddsType.HK.res) + "】"
                    OddsType.MYS.code -> "【" + itemView.context.getString(OddsType.MYS.res) + "】"
                    OddsType.IDN.code -> "【" + itemView.context.getString(OddsType.IDN.res) + "】"
                    else -> "【" + itemView.context.getString(OddsType.EU.res) + "】"
                }
                binding.tvGameTypePlayCate.text = if (row.matchType != null) {
                    //篮球 滚球 全场让分【欧洲盘】
                    "${getGameTypeName(gameType)} ${getMatchTypeName(row.matchType)} ${it.playCateName}$oddsTypeStr"
                } else {
                    "${getGameTypeName(gameType)} ${it.playCateName}$oddsTypeStr"
                }

                binding.llCopyBetOrder.setOnClickListener {
                    itemView.context.copyToClipboard(row.orderNo.orEmpty())
                }
            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
        }

        private fun getMatchTypeName(matchType: String?): String {
            return itemView.context.getString(MatchType.getMatchTypeStringRes(matchType))
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
            binding.tvStatusMoney.text = "${sConfigData?.systemCurrencySign} ${TextUtil.format(data?.win as Double)}"
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
            sportSpinnerList: List<StatusSheetData>,
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

//                tv_title.setTextWithStrokeWidth(context?.getString(R.string.bet_num_and_bet_date) ?: "", 0.7f)

                date_selector.cl_root.layoutParams.height = 40.dp
                sport_selector.cl_root.layoutParams.height = 40.dp

                //sport
                sport_selector.setItemData(sportSpinnerList.toMutableList())

                sport_selector.setSelectCode(nowSelectedSport)

                sport_selector.setOnItemSelectedListener {
                    sportSelectListener.onSelect(it.code)
                }

                //date
                date_selector.setItemData(dateStatusList)

                date_selector.setSelectCode(nowSelectedDate)

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

//    data class TitleBar(val spinnerList: List<StatusSheetData>) : DataItem() {
//        override val orderNum: String = ""
//        override val parlayType = ""
//    }

//    object Footer : DataItem() {
//        override val orderNum: String = ""
//        override val parlayType = ""
//    }

    object NoData : DataItem() {
        override val orderNum: String? = null
        override val parlayType = ""
    }

//    object BackToTop : DataItem() {
//        override val orderNum: String? = null
//        override val parlayType = ""
//    }

}