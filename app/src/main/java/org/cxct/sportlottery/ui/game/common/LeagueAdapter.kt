package org.cxct.sportlottery.ui.game.common

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
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
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.SvgUtil
import org.cxct.sportlottery.util.SvgUtil.defaultIconPath
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class LeagueAdapter(private val matchType: MatchType, var playSelectedCodeSelectionType: Int?, var playSelectedCode: String?) :
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
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_league_v5, parent, false)) //itemview_league_v5
            }

            else -> initBaseViewHolders(parent, viewType)
        }
    }

    fun updateLeagueByPosition(matchId: String?){
        data.forEachIndexed { index, l ->
            l.matchOdds.find { m ->
                m.matchInfo?.id == matchId
            }?.let {
                notifyItemChanged(index, it)
            }
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

    fun updateLeagueBySelectCsTab(position: Int, matchOdd: MatchOdd){
        notifyItemChanged(position, matchOdd)
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
                                    (holder as ItemViewHolder).updateLeagueExpand(data[position], matchType)
                                }
                            }
                        }
                    }

                    // 作用於賠率刷新、波坦tab切換
                    is MatchOdd -> {
                        (holder as ItemViewHolder).updateByMatchIdForOdds(it)
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
        2
    } else {
        data.size + 1
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)

        when (holder) {
            is ItemViewHolder -> {
                holder.itemView.league_odd_list.adapter = null
            }
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val leagueOddAdapter by lazy {
            LeagueOddAdapter2(matchType)
        }

        fun bind(
            item: LeagueOdd,
            matchType: MatchType,
            leagueListener: LeagueListener?,
            leagueOddListener: LeagueOddListener?,
            oddsType: OddsType,
        ) {
            itemView.league_text.text = item.league.name
            val countryIcon = SvgUtil.getSvgDrawable(
                itemView.context,
                if (item.league.categoryIcon.isEmpty()) {
                    defaultIconPath
                } else {
                    item.league.categoryIcon
                }
            )
            itemView.iv_country.setImageDrawable(countryIcon)
            setupLeagueOddList(item, leagueOddListener, oddsType)
            setupLeagueOddExpand(item, matchType, leagueListener)
        }

        // region update functions
        fun update(item: LeagueOdd, matchType: MatchType, oddsType: OddsType) {
            itemView.league_text.text = item.league.name
            val countryIcon = SvgUtil.getSvgDrawable(
                itemView.context,
                if (item.league.categoryIcon.isEmpty()) {
                    defaultIconPath
                } else {
                    item.league.categoryIcon
                }
            )
            itemView.iv_country.setImageDrawable(countryIcon)
            updateLeagueOddList(item, oddsType)
            updateTimer(matchType, item.gameType)
        }
        fun updateByBetInfo() {
            leagueOddAdapter.updateByBetInfo(leagueOddListener?.clickOdd)
        }

        fun updateByPlayCate() {
            leagueOddAdapter.updateByPlayCate()
        }

        fun updateByMatchIdForOdds(matchOdd: MatchOdd){
            if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                leagueOddAdapter.updateByMatchIdForOdds(matchOdd)
            }
        }

        fun updateBySelectCsTab(matchOdd: MatchOdd){
            leagueOddAdapter.updateBySelectCsTab(matchOdd)
        }

        private fun updateLeagueOddList(item: LeagueOdd, oddsType: OddsType) {
            leagueOddAdapter.data = if (item.searchMatchOdds.isNotEmpty()) {
                item.searchMatchOdds
            } else {
                item.matchOdds
            }.onEach {
                it.matchInfo?.gameType = item.gameType?.key
            }
            leagueOddAdapter.oddsType = oddsType
            leagueOddAdapter.playSelectedCodeSelectionType = playSelectedCodeSelectionType
            leagueOddAdapter.playSelectedCode = playSelectedCode
            if (itemView.league_odd_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !itemView.league_odd_list.isComputingLayout) {
                leagueOddAdapter.update()
            }
        }

        fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            val position = bindingAdapterPosition
            val positionData = data[position]
            expandCheckList[positionData.league.id].apply {
                if (this != null) {
                    positionData.unfoldStatus = if (this == true) FoldState.UNFOLD.code else FoldState.FOLD.code
                }
            }
            itemView.league_odd_list.visibility = if (positionData.unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
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
                adapter = leagueOddAdapter.apply {
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
                                R.drawable.divider_color_white8
                            )
                        )
                    ) // TODO IllegalStateException: Cannot add item decoration during a scroll  or layout
                } catch (e: Exception) {
                }
            }
        }

        private fun setupLeagueOddExpand(item: LeagueOdd, matchType: MatchType, leagueListener: LeagueListener?) {
            val position = bindingAdapterPosition
            val positionData = data[position]
            expandCheckList[positionData.league.id].apply {
                if (this != null) {
                    positionData.unfoldStatus = if (this == true) FoldState.UNFOLD.code else FoldState.FOLD.code
                }
            }

            itemView.league_odd_list.visibility = if (positionData.unfoldStatus == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)

            itemView.setOnClickListener {
                if (position > data.size - 1) return@setOnClickListener
                positionData.unfoldStatus = if (positionData.unfoldStatus == FoldState.UNFOLD.code) {
                    expandCheckList[positionData.league.id] = false
                    FoldState.FOLD.code
                } else {
                    expandCheckList[positionData.league.id] = true
                    FoldState.UNFOLD.code
                } // TODO IndexOutOfBoundsException: Index: 10, Size: 5
                updateTimer(matchType, item.gameType)

//                notifyItemChanged(adapterPosition)
                updateLeagueByExpand(bindingAdapterPosition)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_odd_list.visibility == View.VISIBLE && (gameType == GameType.FT || gameType == GameType.BK || gameType == GameType.RB || gameType == GameType.AFT || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }
    }

}

class LeagueListener(val clickListenerLeague: (item: LeagueOdd) -> Unit) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
}