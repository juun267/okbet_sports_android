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
import java.lang.ref.WeakReference
import java.util.*

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
        }

        private fun getSportRootCache(): RecyclerView.RecycledViewPool {
            var cache = rootCachePool?.get()
            if (cache == null) {
                cache = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(ItemType.ITEM.ordinal,15) }
                rootCachePool = WeakReference(cache)
            }
            return cache
        }

        private fun getOddListCache(): RecyclerView.RecycledViewPool {
            var cache = oddListCachePool?.get()
            if (cache == null) {
                cache = RecyclerView.RecycledViewPool().apply { setMaxRecycledViews(ItemType.ITEM.ordinal,50) }
                oddListCachePool = WeakReference(cache)
            }
            return cache
        }

        private fun getOddButtonCache(): RecyclerView.RecycledViewPool {
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

    private fun refreshByBetInfo() {
        lifecycle.lifecycleScope.launch(Dispatchers.IO) {

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

            withContext(Dispatchers.Main) {
                data.forEachIndexed { index, leagueOdd ->
                    updateLeague(index, leagueOdd)
                }
            }
        }
    }

    var lastBetInfoSize = 0
    var betInfoList: MutableList<BetInfoListData> = mutableListOf()
        set(value) {

            // 会重复设置的问题
            if (field == value && lastBetInfoSize == value.size) {
                return
            }
            lastBetInfoSize = value.size
            field = value
            if (leagueOddListener?.clickOdd == null) {
                refreshByBetInfo()
                return
            }

            data.forEachIndexed { index, leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap?.values?.forEachIndexed {i, oddList ->
                        oddList?.forEachIndexed {j, odd ->
                            odd?.isSelected = field.any { betInfoListData ->
                                betInfoListData.matchOdd.oddsId == odd?.id
                            }
                            if (leagueOddListener?.clickOdd?.id == odd?.id) {
                                updateLeague(index, leagueOdd)
                                return@forEach
                            }
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
                val itemHolder = ItemViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_league, parent, false)) //itemview_league_v5
                itemHolder.itemView.league_odd_list.setRecycledViewPool(getOddListCache())
                itemHolder
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

    fun updateMatchOdd(position: Int, payload: MatchOdd) {
        notifyItemChanged(position, PayLoadEnum.PAYLOAD_ODDS)
    }

    private fun updateLeagueByBetInfo(position: Int) {
        notifyItemChanged(position, PayLoadEnum.PAYLOAD_BET_INFO)
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
            oddsType: OddsType,
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

}
