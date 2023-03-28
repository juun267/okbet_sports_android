package org.cxct.sportlottery.ui.sport.favorite

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_favorite.view.*
import kotlinx.android.synthetic.main.item_favorite.view.iv_arrow
import kotlinx.android.synthetic.main.item_league.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.PayLoadEnum
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class FavoriteAdapter(private val matchType: MatchType) :
    BaseGameAdapter() {

    //缓存最新更新的赔率列表，用来比较判断是否需要更新
    var lastOddIds = mutableListOf<String>()
    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            var newOddsIds = value.map { it.matchOdd.oddsId }.toMutableList()
            var needUpdate = lastOddIds != newOddsIds
            if (needUpdate) {
                field = value
                lastOddIds = newOddsIds
                data.forEachIndexed { index, leagueOdd ->
                    leagueOdd.matchOdds.forEach { matchOdd ->
                        var needUpdateMatch = false
                        matchOdd.oddsMap?.values?.forEach { odds ->
                            odds?.forEach { odd ->
                                val betInfoSelected = betInfoList.any { betInfoListData ->
                                    betInfoListData.matchOdd.oddsId == odd?.id
                                }
                                if (odd?.isSelected != betInfoSelected) {
                                    odd?.isSelected = betInfoSelected
                                    needUpdateMatch = true
                                }
                            }
                        }
                        if (needUpdateMatch) {
                            updateMatch(index, matchOdd)
                        }
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

    fun removePreloadItem() {
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
                    .inflate(R.layout.item_favorite, parent, false)) //itemview_league_v5
            }

            else -> initBaseViewHolders(parent, viewType)
        }
    }

    // region update by payload functions
    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    fun updateMatch(position: Int, payload: MatchOdd) {
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

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            //if(holder is ItemViewHolder) holder.update(data[position], matchType, oddsType)
        } else {
            // Update with payload
                payloads.forEach {
                    when (it) {
                        is LeagueOdd -> {
                            val leagueOdd = payloads.first() as LeagueOdd
                            (holder as FavoriteAdapter.ItemViewHolder).update(leagueOdd,
                                matchType,
                                oddsType)
                        }
                        is MatchOdd -> {
                            (holder as FavoriteAdapter.ItemViewHolder).update(it,
                                matchType,
                                oddsType)
                        }
                        is PayLoadEnum -> {
                            when (it) {
                                PayLoadEnum.PAYLOAD_BET_INFO -> {
                                    (holder as FavoriteAdapter.ItemViewHolder).updateByBetInfo()
                                }
                                PayLoadEnum.PAYLOAD_PLAYCATE -> {
                                    (holder as FavoriteAdapter.ItemViewHolder).updateByPlayCate()
                                }
                                PayLoadEnum.EXPAND -> {
                                    (holder as FavoriteAdapter.ItemViewHolder).updateLeagueExpand(
                                        data[position],
                                        matchType)
                                }
                            }
                        }
                        // 作用於賠率刷新、波坦tab切換
                        is MatchOdd -> {
                            (holder as FavoriteAdapter.ItemViewHolder).updateByMatchIdForOdds(it)
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

    override fun getItemCount(): Int = data.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ItemViewHolder -> {
                holder.itemView.rv_league.adapter = null
            }
        }
    }

    fun updateLeagueBySelectCsTab(position: Int, matchOdd: MatchOdd) {
        notifyItemChanged(position, matchOdd)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val sportFavoriteAdapter by lazy {
            SportFavoriteAdapter(matchType)
        }

        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
        ) {
            val position = data.indexOf(item)
            var gameType =
                if (item.gameType != null) item.gameType!!.key else item.matchOdds[0].matchInfo?.gameType!!
            itemView.lin_sport_title.isVisible =
                (position == 0 || data[position - 1].gameType != item.gameType)
            itemView.tv_sport_type.text =
                GameType.getGameTypeString(itemView.context, gameType)
            itemView.iv_sport.setImageResource(GameType.getGameTypeMenuIcon(gameType))
            itemView.iv_sport.isSelected = true
            itemView.iv_arrow.isSelected = item.unfoldStatus != FoldState.FOLD.code
            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        // region update functions
        fun update(item: LeagueOdd, matchType: MatchType, oddsType: OddsType) {
            var gameType =
                if (item.gameType != null) item.gameType!!.key else item.matchOdds[0].matchInfo?.gameType!!
            val position = data.indexOf(item)
            itemView.lin_sport_title.isVisible =
                (position == 0 || data[position - 1].gameType != item.gameType)
            itemView.tv_sport_type.text =
                GameType.getGameTypeString(itemView.context, gameType)
            itemView.iv_sport.setImageResource(GameType.getGameTypeMenuIcon(gameType))
            itemView.iv_sport.isSelected = true
            itemView.iv_arrow.isSelected = item.unfoldStatus != FoldState.FOLD.code
            updateLeagueOddList(item, oddsType)
            updateTimer(matchType, item.gameType)
        }

        fun update(item: MatchOdd, matchType: MatchType, oddsType: OddsType) {
            sportFavoriteAdapter.oddsType = oddsType
            sportFavoriteAdapter.data.forEachIndexed { index, matchOdd ->
                if (item.matchInfo?.id == matchOdd.matchInfo?.id) {
                    if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                        sportFavoriteAdapter.notifyItemChanged(index, item)
                    }
                }
            }
        }

        fun updateByBetInfo() {
            sportFavoriteAdapter.updateByBetInfo(leagueOddListener?.clickOdd)
        }

        fun updateByPlayCate() {
            sportFavoriteAdapter.updateByPlayCate()
        }

        fun updateByMatchIdForOdds(matchOdd: MatchOdd) {
            if (itemView.rv_league.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.rv_league.isComputingLayout) {
                sportFavoriteAdapter.updateByMatchIdForOdds(matchOdd)
            }
        }

        private fun updateLeagueOddList(item: LeagueOdd, oddsType: OddsType) {
            sportFavoriteAdapter.data = if (item.searchMatchOdds.isNotEmpty()) {
                item.searchMatchOdds
            } else {
                item.matchOdds
            }.onEach {
                it.matchInfo?.leagueName = item.league.name
                it.matchInfo?.gameType = item.gameType?.key
                it.matchInfo?.categoryIcon = item.league.categoryIcon
            }
            sportFavoriteAdapter.oddsType = oddsType
            if (itemView.rv_league.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.rv_league.isComputingLayout) {
                sportFavoriteAdapter.update()
            }
        }

        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            expandCheckList[data[adapterPosition].league.id].apply {
                if (this != null) {
                    data[adapterPosition].unfoldStatus =
                        if (this == true) FoldState.UNFOLD.code else FoldState.FOLD.code
                }
            }
            itemView.rv_league.visibility =
                if (data[adapterPosition].unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
        ) {
            itemView.rv_league.apply {
                //league_odd_list.itemAnimator = null
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = sportFavoriteAdapter.apply {
                    setData(item.searchMatchOdds.ifEmpty {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.leagueName = item.league.name
                        it.matchInfo?.gameType = item.gameType?.key
                        it.matchInfo?.categoryIcon = item.league.categoryIcon
                    }, oddsType)

                    this.leagueOddListener = leagueOddListener
                }
            }
        }

        private fun setupLeagueOddExpand(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
        ) {
            expandCheckList[data[adapterPosition].league.id].apply {
                if (this != null) {
                    data[adapterPosition].unfoldStatus =
                        if (this == true) FoldState.UNFOLD.code else FoldState.FOLD.code
                }
            }

            itemView.rv_league.visibility =
                if (data[adapterPosition].unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)

            itemView.setOnClickListener {
                if (adapterPosition > data.size - 1) return@setOnClickListener
                data[adapterPosition].unfoldStatus =
                    if (data[adapterPosition].unfoldStatus == FoldState.UNFOLD.code) {
                        expandCheckList[data[adapterPosition].league.id] = false
                        FoldState.FOLD.code
                    } else {
                        expandCheckList[data[adapterPosition].league.id] = true
                        FoldState.UNFOLD.code
                    } // TODO IndexOutOfBoundsException: Index: 10, Size: 5

                itemView.iv_arrow.isSelected = item.unfoldStatus != FoldState.FOLD.code
                updateTimer(matchType, item.gameType)
                data.forEachIndexed { index, leagueOdd ->
                    if (leagueOdd.gameType == data[adapterPosition].gameType) {
                        leagueOdd.unfoldStatus = data[adapterPosition].unfoldStatus
                    }
                    updateLeagueByExpand(index)
                }
                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            sportFavoriteAdapter.isTimerEnable =
                itemView.rv_league.visibility == View.VISIBLE && (gameType == GameType.FT || gameType == GameType.BK || gameType == GameType.RB || gameType == GameType.AFT || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }
    }

}

class LeagueListener(val clickListenerLeague: (item: LeagueOdd) -> Unit) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
}