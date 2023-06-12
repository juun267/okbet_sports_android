package org.cxct.sportlottery.ui.betRecord.accountHistory.next

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentBinding
import org.cxct.sportlottery.databinding.ItemAccountHistoryNextContentParlayBinding
import org.cxct.sportlottery.network.bet.settledDetailList.MatchOdd
import org.cxct.sportlottery.network.bet.settledDetailList.Other
import org.cxct.sportlottery.network.bet.settledDetailList.Row
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.ui.betRecord.BetRecordEndScoreAdapter
import org.cxct.sportlottery.ui.betRecord.ParlayType
import org.cxct.sportlottery.ui.betRecord.detail.BetDetailsActivity
import org.cxct.sportlottery.ui.common.adapter.StatusSheetData
import org.cxct.sportlottery.util.*
import org.cxct.sportlottery.view.onClick


class AccountHistoryNextAdapter(private val itemClickListener: ItemClickListener
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
                holder.bind(data.row, oddsType, position, itemClickListener)
            }

            is OutrightItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType, position, itemClickListener)
            }

            is ParlayItemViewHolder -> {
                val data = getItem(position) as DataItem.Item
                holder.bind(data.row, oddsType, position, itemClickListener)
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

        fun bind(row: Row, oddsType: OddsType, position: Int, itemClickListener: ItemClickListener) {
            binding.topViewParlay.isVisible = position == 0

            val parlayAdapter by lazy { ParlayItemAdapter() }

            binding.row = row

            binding.apply {
                matchOdd = row.matchOdds?.firstOrNull()

                matchOdd?.apply {
                    binding.clickableView.setOnClickListener {
                        itemClickListener.onClick(row, this)
                    }
                }

                row.parlayType?.let { parlayType ->
                    ParlayType.getParlayStringRes(parlayType)?.let { parlayTypeStringResId ->
//                        tvParlayType.text = LocalUtils.getString(parlayTypeStringResId)
                        val parlayTitle = itemView.context.getString(R.string.bet_record_parlay) +
                                "(${itemView.context.getString(parlayTypeStringResId)})" +
                                "-${GameType.getGameTypeString(itemView.context, row.gameType)}"
                        tvParlayType.text = parlayTitle
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
                    parlayAdapter.matchType = row.matchType
                }

                llCopyBetOrderParlay.setOnClickListener {
                    itemView.context.copyToClipboard(row.orderNo.orEmpty())
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
        fun bind(row: Row, oddsType: OddsType, position: Int, itemClickListener: ItemClickListener) {
            binding.topView.isVisible = position == 0

            val first = row.matchOdds?.firstOrNull()

            binding.row = row
            binding.matchOdd = first

            first?.apply {
                binding.root.setOnClickListener {
                    itemClickListener.onClick(row, first)
                }

                first.oddsType?.let {
                    val formatForOdd = if (playCateCode == PlayCate.LCS.value) TextUtil.formatForOddPercentage((odds ?: 0.0) - 1.0) else TextUtil.formatForOdd(odds ?: 0)
                    binding.tvContent.setPlayContent(
                        playName,
                        spread,
                        formatForOdd
                    )
                }

                val singleTitle = itemView.context.getString(R.string.bet_record_single) +
                        "-${GameType.getGameTypeString(itemView.context, row.gameType)}"
                binding.tvTitle.text = singleTitle

                //篮球 滚球 全场让分【欧洲盘】
                binding.tvGameTypePlayCate.setGameType_MatchType_PlayCateName_OddsType(
                    row.gameType,
                    row.matchType,
                    this.playCateName,
                    this.oddsType
                )

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

        fun bind(row: Row, oddsType: OddsType, position: Int, itemClickListener: ItemClickListener) {
            binding.topView.isVisible = position == 0

            val first = row.matchOdds?.firstOrNull()

            val roundAdapter by lazy { RoundAdapter() }

            binding.row = row
            binding.matchOdd = first

            first?.let { it ->
                binding.root.setOnClickListener {
                    itemClickListener.onClick(row, first)
                }
                val playName =
                    if (it.playCateCode == PlayCate.FS_LD_CS.value)
                        itemView.context.getString(R.string.N903)
                    else it.playName
                val formatForOdd =
                    when (it.playCateCode) {
                        PlayCate.LCS.value -> TextUtil.formatForOddPercentage((it.odds
                            ?: 0.0) - 1.0)
                        else -> TextUtil.formatForOdd(it.odds ?: 0)
                    }
                binding.tvContent.setPlayContent(
                    playName,
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

                val singleTitle = itemView.context.getString(R.string.bet_record_single) +
                        "-${GameType.getGameTypeString(itemView.context, row.gameType)}"
                binding.tvTitle.text = singleTitle

                binding.tvTeamNamesSingles.setTeamsNameWithVS(it.homeName, it.awayName)

                //篮球 滚球 全场让分【欧洲盘】
                binding.tvGameTypePlayCate.setGameType_MatchType_PlayCateName_OddsType(
                    row.gameType,
                    row.matchType,
                    it.playCateName,
                    it.oddsType
                )

                binding.llCopyBetOrder.setOnClickListener {
                    itemView.context.copyToClipboard(row.orderNo.orEmpty())
                }

                binding.linEndscoreParent.apply {
                    linEndscore.isVisible =
                        row.matchOdds.firstOrNull()?.playCateCode == PlayCate.FS_LD_CS.value
                    if (linEndscore.isVisible) {
                        itemView.onClick {
                            val intent = Intent(itemView.context, BetDetailsActivity::class.java)
                            intent.putExtra("detailRow", row)
                            itemView.context.startActivity(intent)
                        }
                        val listData = if (row.matchOdds.firstOrNull()?.multiCode?.size ?: 0 > 6) {
                            row.matchOdds.firstOrNull()?.multiCode?.subList(0, 6)
                        } else {
                            row.matchOdds.firstOrNull()?.multiCode ?: listOf()
                        }
                        if (rvEndscoreInfo.adapter == null) {
                            rvEndscoreInfo.layoutManager =
                                LinearLayoutManager(rvEndscoreInfo.context,
                                    RecyclerView.HORIZONTAL,
                                    false)
                            rvEndscoreInfo.addItemDecoration(SpaceItemDecoration(itemView.context,
                                R.dimen.margin_4))
                            val scoreAdapter = BetRecordEndScoreAdapter()
                            rvEndscoreInfo.adapter = scoreAdapter
                            scoreAdapter.setList(listData)
                        } else {
                            (rvEndscoreInfo.adapter as BetRecordEndScoreAdapter).setList(listData)
                        }
                        tvMore?.let {
                            tvMore.isVisible = row.matchOdds.firstOrNull()?.multiCode?.size ?: 0 > 6
                        }

                    }
                }

            }

            binding.executePendingBindings() //加上這句之後數據每次丟進來時才能夠即時更新
        }

        private fun getGameTypeName(gameType: String): String {
            return itemView.context.getString(GameType.valueOf(gameType).string)
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

class ItemClickListener(val clickListener: (data: Row, matchData: MatchOdd) -> Unit) {
    fun onClick(data: Row, matchData: MatchOdd) = clickListener(data, matchData)
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