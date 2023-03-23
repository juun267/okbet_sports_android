package org.cxct.sportlottery.ui.sport

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_league.view.*
import kotlinx.coroutines.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.PayLoadEnum
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.ui.base.BaseGameAdapter
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.game.common.LeagueOddListener
import org.cxct.sportlottery.ui.game.common.view.OddsButton2
import org.cxct.sportlottery.common.OddsType
import org.cxct.sportlottery.ui.sport.favorite.LeagueListener
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.setLeagueLogo
import java.lang.ref.WeakReference

@SuppressLint("NotifyDataSetChanged")
class SportLeagueAdapter(val lifecycle: LifecycleOwner, private val matchType: MatchType) :
    BaseGameAdapter() {

    companion object {

        private var rootCachePool: WeakReference<RecyclerView.RecycledViewPool>? = null
        private var oddListCachePool: WeakReference<RecyclerView.RecycledViewPool>? = null
        private var oddBtnCachePool: WeakReference<RecyclerView.RecycledViewPool>? = null

        fun clearCachePool() {
            rootCachePool = null
            oddListCachePool = null
            oddListCachePool = null
            OddsButton2.clearOddsViewCaches()
        }

        private fun getSportRootCache(): RecyclerView.RecycledViewPool {
            var cache = rootCachePool?.get()
            if (cache == null) {
                cache = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(ItemType.ITEM.ordinal,25) }
                rootCachePool = WeakReference(cache)
            }
            return cache
        }

         fun getOddListCache(): RecyclerView.RecycledViewPool {
            var cache = oddListCachePool?.get()
            if (cache == null) {
                cache = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(ItemType.ITEM.ordinal,50) }
                oddListCachePool = WeakReference(cache)
            }
            return cache
        }

         fun getOddButtonCache(): RecyclerView.RecycledViewPool {
            var cache = oddBtnCachePool?.get()
            if (cache == null) {
                cache = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(ItemType.ITEM.ordinal,150) }
                oddBtnCachePool = WeakReference(cache)
            }
            return cache
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)


//        recyclerView.setRecycledViewPool(getSportRootCache())  // 对局部刷新有影响
    }

    //缓存最新更新的赔率列表，用来比较判断是否需要更新
    var lastOddIds = mutableListOf<String>()
    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {
            var newOddsIds = value.map { it.matchOdd.oddsId }.toMutableList()
            var needUpdate = lastOddIds != newOddsIds
            if (needUpdate) {
                field = value
                lastOddIds = newOddsIds
                lifecycle.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        data.asIterable().forEachIndexed { index, leagueOdd ->
                            leagueOdd.matchOdds.asIterable().forEach { matchOdd ->
                                var needUpdateMatch = false
                                matchOdd.oddsMap?.values?.asIterable()?.forEach { odds ->
                                    odds?.asIterable()?.forEach { odd ->
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
                                    withContext(Dispatchers.Main) {
                                        updateMatch(index, matchOdd)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    enum class ItemType {
        ITEM, NO_DATA
    }

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

    // region update by payload functions
    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    fun updateMatch(position: Int, payload: MatchOdd) {
        notifyItemChanged(position, payload)
    }

    fun updateMatchOdd(position: Int, payload: MatchOdd) {
        notifyItemChanged(position, PayLoadEnum.PAYLOAD_ODDS)
    }

    private fun updateLeagueByBetInfo(position: Int) {
        notifyItemChanged(position, PayLoadEnum.PAYLOAD_BET_INFO)
    }

    fun updateLeagueByExpand(position: Int) {
        notifyItemChanged(position, PayLoadEnum.EXPAND)
    }

    private var refreshTime = 0L

    // 限制全列表更新頻率
    fun limitRefresh() {
        val time = System.currentTimeMillis()
        if (time - refreshTime > 1000) {
            refreshTime = time
            Log.d("Hewie", "UpdateAll...")
            notifyDataSetChanged()
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
                        (holder as ItemViewHolder).update(it, matchType, oddsType)
                    }
                    is MatchOdd -> {
                        (holder as ItemViewHolder).update(it, matchType, oddsType)
                    }
                    is PayLoadEnum -> {
                        when (it) {
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
                            PayLoadEnum.PAYLOAD_MATCH_CLOCK -> {
                                (holder as ItemViewHolder).updateLeagueExpand(data[position],
                                    matchType)
                            }
                            PayLoadEnum.PAYLOAD_ODDS -> {
                                (holder as ItemViewHolder).updateLeagueExpand(data[position],
                                    matchType)
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

    fun updateLeagueBySelectCsTab(position: Int, matchOdd: MatchOdd) {
        notifyItemChanged(position, matchOdd)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val sportOddAdapter by lazy {
            SportOddAdapter(matchType, getOddButtonCache())
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
            itemView.iv_arrow.isSelected = item.unfoldStatus == FoldState.FOLD.code
            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        // region update functions
        fun update(item: LeagueOdd, matchType: MatchType, oddsType: OddsType) {
            itemView.league_text.text = item.league.name
            itemView.iv_country.setLeagueLogo(item.league.categoryIcon)
            itemView.iv_arrow.isSelected = item.unfoldStatus == FoldState.FOLD.code
            updateLeagueOddList(item, oddsType)
            updateTimer(matchType, item.gameType)
        }

        fun update(item: MatchOdd, matchType: MatchType, oddsType: OddsType) {
            updateMatchOdds(item, oddsType)
            updateTimer(matchType, GameType.getGameType(item.matchInfo?.gameType))
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

        private fun updateMatchOdds(item: MatchOdd, oddsType: OddsType) {
            sportOddAdapter.oddsType = oddsType
            sportOddAdapter.data.forEachIndexed { index, matchOdd ->
                if (item.matchInfo?.id == matchOdd.matchInfo?.id) {
                    if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                        sportOddAdapter.updateIndex(index, item)
                    }
                }
            }
        }

        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            itemView.league_odd_list.visibility =
                if (item.unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)
        }

        private fun setupLeagueOddList(
            item: LeagueOdd,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType
        ) {
            itemView.league_odd_list.apply {
                //league_odd_list.itemAnimator = null

                adapter = sportOddAdapter.apply {
                    setData(item.searchMatchOdds.ifEmpty {
                        item.matchOdds
                    }.onEach {
                        it.matchInfo?.leagueName = item.league.name
                        it.matchInfo?.gameType = item.gameType?.key
                    }, oddsType)

                    this.leagueOddListener = leagueOddListener
                }

            }
        }

        private fun setupLeagueOddExpand(item: LeagueOdd, matchType: MatchType, leagueListener: LeagueListener?) {

            val position = bindingAdapterPosition
            itemView.league_odd_list.visibility =
                if (item.unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)

            itemView.setOnClickListener {
                if (position > data.size - 1) return@setOnClickListener
                item.unfoldStatus = if (item.unfoldStatus == FoldState.UNFOLD.code) {
                    FoldState.FOLD.code
                } else {
                    FoldState.UNFOLD.code
                } // TODO IndexOutOfBoundsException: Index: 10, Size: 5
                itemView.iv_arrow.isSelected = item.unfoldStatus == FoldState.FOLD.code
                updateTimer(matchType, item.gameType)
                updateLeagueByExpand(position)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            sportOddAdapter.isTimerEnable =
                itemView.league_odd_list.visibility == View.VISIBLE && (gameType == GameType.FT || gameType == GameType.BK || gameType == GameType.RB || gameType == GameType.AFT || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType != ItemType.ITEM.ordinal) {
            return initBaseViewHolders(parent, viewType)
        }

        val itemHolder = ItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_league, parent, false)) //itemview_league_v5
        itemHolder.itemView.league_odd_list.apply {
            setRecycledViewPool(getOddListCache())
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecorator(ContextCompat.getDrawable(context, R.drawable.bg_sport_divide_line)))
        }

        return itemHolder
    }


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        if (holder is ItemViewHolder) {
            holder.itemView.league_odd_list.adapter = null
        }
    }


}
