package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.enum.PayLoadEnum
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.ui.sport.favorite.LeagueListener
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.setLeagueLogo
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class SportLeagueAdapter(private val matchType: MatchType) :
    BaseGameAdapter() {

    private fun refreshByBetInfo() {
        data.forEach { leagueOdd ->
            leagueOdd.matchOdds.forEach { matchOdd ->
                matchOdd.oddsMap?.values?.forEach { oddList ->
                    oddList?.forEach { odd ->
                        odd?.isSelected = betInfoList.any { betInfoListData ->
                            betInfoListData.matchOdd.oddsId == odd?.id
                        }
                    }
                }
                matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                    quickPlayCate.quickOdds.forEach { map ->
                        map.value?.forEach { odd ->
                            odd?.isSelected = betInfoList.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                        }
                    }
                }
            }
        }
        data.forEachIndexed { index, leagueOdd -> updateLeague(index, leagueOdd) }
    }

    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            field = value
            if (leagueOddListener?.clickOdd == null) {
                refreshByBetInfo()
                return
            }

            var isInMatch = false
            var isInQuick = false
            data.forEachIndexed { index, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap?.values?.forEach { oddList ->
                        oddList?.forEach { odd ->
                            odd?.isSelected = field.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                            if (leagueOddListener?.clickOdd?.id == odd?.id) {
                                isInMatch = true
                            }
                        }
                    }
                    matchOdd.quickPlayCateList?.forEach { quickPlayCate ->
                        quickPlayCate.quickOdds.forEach { map ->
                            map.value?.forEach { odd ->
                                odd?.isSelected = field.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                                if (leagueOddListener?.clickOdd?.id == odd?.id) {
                                    isInQuick = true
                                }
                            }
                        }
                    }
                    if (isInMatch || isInQuick) {
                        updateLeagueByBetInfo(index)
                        isInMatch = false
                        isInQuick = false
                    }
                }
            }
        }

    enum class ItemType {
        ITEM, NO_DATA
    }

    var isLock = true
    var mTimer = Timer()

    var data = mutableListOf<LeagueOdd>()
        set(value) {
            field = value
            isPreload = false
        }

    var discount: Float = 1.0F
        set(value) {
            if (field == value) return
            data.forEachIndexed { index, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap?.updateOddsDiscount(field, value)
                }

                updateLeague(index, leagueOdd)
            }

            field = value
        }

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    var leagueListener: LeagueListener? = null

    var leagueOddListener: LeagueOddListener? = null

    fun setPreloadItem() {
        data.clear()
        isPreload = true
        notifyDataSetChanged()
    }

    fun removePreloadItem(){
        data = mutableListOf()
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (isPreload) {
            return BaseItemType.PRELOAD_ITEM.type
        }
        return when {
            data.isEmpty() -> BaseItemType.NO_DATA.type
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> {
                ItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_league, parent, false)) //itemview_league_v5
            }

            else -> initBaseViewHolders(parent, viewType)
        }
    }

    // region update by payload functions
    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    private fun updateLeagueByBetInfo(position: Int) {
        notifyItemChanged(position, PayLoadEnum.PAYLOAD_BET_INFO)
    }

    fun updateLeagueByPlayCate() {
        data.forEachIndexed { index, _ ->
            notifyItemChanged(index, PayLoadEnum.PAYLOAD_PLAYCATE)
        }
    }

    fun updateLeagueByExpand(position: Int) {
        notifyItemChanged(position, PayLoadEnum.EXPAND)
    }

    // 限制全列表更新頻率
    fun limitRefresh() {
        if (isLock) {
            Log.d("Hewie", "UpdateAll...")
            isLock = false
            notifyDataSetChanged()
            mTimer.schedule(object : TimerTask() {
                override fun run() {
                    isLock = true
                }
            }, 1000)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            //if(holder is ItemViewHolder) holder.update(data[position], matchType, oddsType)
        } else {
            // Update with payload
            payloads.forEach {
                when (it) {
                    is LeagueOdd -> {
                        val leagueOdd = payloads.first() as LeagueOdd
                        (holder as ItemViewHolder).update(leagueOdd, matchType, oddsType)
                    }

                    is PayLoadEnum -> {
                        it.apply {
                            when (this) {
                                PayLoadEnum.PAYLOAD_BET_INFO -> {
                                    (holder as ItemViewHolder).updateByBetInfo()
                                }
                                PayLoadEnum.PAYLOAD_PLAYCATE -> {
                                    (holder as ItemViewHolder).updateByPlayCate()
                                }
                                PayLoadEnum.EXPAND -> {
                                    (holder as ItemViewHolder).updateLeagueExpand(data[position],
                                        matchType)
                                }
                            }
                        }
                    }
                    // 作用於賠率刷新、波坦tab切換
                    is MatchOdd -> {
                        (holder as SportLeagueAdapter.ItemViewHolder).updateByMatchIdForOdds(it)
                    }
                }
            }
        }
    }
    // endregion

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]
                holder.bind(
                    item,
                    matchType,
                    leagueListener,
                    leagueOddListener,
                    oddsType
                )
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
    } else {
        data.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ItemViewHolder -> {
                holder.itemView.league_odd_list.adapter = null
            }
        }
    }

    fun updateLeagueBySelectCsTab(position: Int, matchOdd: MatchOdd) {
        notifyItemChanged(position, matchOdd)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val sportOddAdapter by lazy {
            SportOddAdapter(matchType)
        }

        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
        ) {
            itemView.league_text.text = item.league.name
            itemView.iv_country.setLeagueLogo(item.league.categoryIcon)
            itemView.iv_arrow.isSelected = item.unfold == FoldState.FOLD.code
            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        // region update functions
        fun update(item: LeagueOdd, matchType: MatchType, oddsType: OddsType) {
            itemView.league_text.text = item.league.name
            itemView.iv_country.setLeagueLogo(item.league.categoryIcon)
            itemView.iv_arrow.isSelected = item.unfold == FoldState.FOLD.code
            updateLeagueOddList(item, oddsType)
            updateTimer(matchType, item.gameType)
        }

        fun updateByBetInfo() {
            sportOddAdapter.updateByBetInfo(leagueOddListener?.clickOdd)
        }

        fun updateByPlayCate() {
            sportOddAdapter.updateByPlayCate()
        }

        fun updateByMatchIdForOdds(matchOdd: MatchOdd) {
            if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                sportOddAdapter.updateByMatchIdForOdds(matchOdd)
            }
        }

        fun updateBySelectCsTab(matchOdd: MatchOdd) {
            sportOddAdapter.updateBySelectCsTab(matchOdd)
        }

        private fun updateLeagueOddList(item: LeagueOdd, oddsType: OddsType) {
            sportOddAdapter.data = if (item.searchMatchOdds.isNotEmpty()) {
                item.searchMatchOdds
            } else {
                item.matchOdds
            }.onEach {
                it.matchInfo?.gameType = item.gameType?.key
            }
            sportOddAdapter.oddsType = oddsType
            if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                sportOddAdapter.update()
            }
        }

        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            itemView.league_odd_list.visibility =
                if (item.unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                //league_odd_list.itemAnimator = null
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = sportOddAdapter.apply {
                    setData(item.searchMatchOdds.ifEmpty {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.leagueName = item.league.name
                        it.matchInfo?.gameType = item.gameType?.key
                    }, oddsType)

                    this.leagueOddListener = leagueOddListener
                }
                try {
                    addItemDecoration(
                        DividerItemDecorator(
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.bg_sport_divide_line
                            )
                        )
                    ) // TODO IllegalStateException: Cannot add item decoration during a scroll  or layout
                } catch (e: Exception) {
                }
            }
        }

        private fun setupLeagueOddExpand(item: LeagueOdd, matchType: MatchType, leagueListener: LeagueListener?) {

            itemView.league_odd_list.visibility =
                if (item.unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)

            itemView.setOnClickListener {
                if (adapterPosition > data.size - 1) return@setOnClickListener
                item.unfold = if (item.unfold == FoldState.UNFOLD.code) {
                    FoldState.FOLD.code
                } else {
                    FoldState.UNFOLD.code
                } // TODO IndexOutOfBoundsException: Index: 10, Size: 5
                itemView.iv_arrow.isSelected = item.unfold == FoldState.FOLD.code
                updateTimer(matchType, item.gameType)
                updateLeagueByExpand(bindingAdapterPosition)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            sportOddAdapter.isTimerEnable =
                itemView.league_odd_list.visibility == View.VISIBLE && (gameType == GameType.FT || gameType == GameType.BK || gameType == GameType.RB || gameType == GameType.AFT || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }
    }

}
