package org.cxct.sportlottery.ui.game.common

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_game_v3.*
import kotlinx.android.synthetic.main.itemview_league_v5.view.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.common.DividerItemDecorator
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.ExpandCheckListManager.expandCheckList
import org.cxct.sportlottery.util.MatchOddUtil.updateOddsDiscount
import org.cxct.sportlottery.util.SvgUtil
import org.cxct.sportlottery.util.SvgUtil.defaultIconPath
import java.util.*

const val BET_INFO: String = "bet-info"

class LeagueAdapter(private val matchType: MatchType, var playSelectedCodeSelectionType: Int?, var playSelectedCode: String?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                            if (leagueOddListener?.clickOdd == odd) {
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
                                if (leagueOddListener?.clickOdd == odd) {
                                    isInQuick = true
                                }
                            }
                        }
                    }
                    if (isInMatch || isInQuick) {
                        updateLeagueByBetInfo(index, BET_INFO)
                        isInMatch = false
                        isInQuick = false
                    }
                }
            }
        }

    enum class ItemType {
        ITEM, NO_DATA, BOTTOM_NAVIGATION
    }

    var isLock = true
    var mTimer = Timer()

    var data = mutableListOf<LeagueOdd>()
        set(value) {
            field = value
            //notifyDataSetChanged()
        }

    var discount: Float = 1.0F
        set(value) {
            if (field == value) return

            data.forEach { leagueOdd ->
                leagueOdd.matchOdds.forEach { matchOdd ->
                    matchOdd.oddsMap?.updateOddsDiscount(field, value)
                }
            }

            field = value
            //notifyDataSetChanged()
        }

    var searchText = ""
        set(value) {
            field = value
            //notifyDataSetChanged()
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

    override fun getItemViewType(position: Int): Int {
        return when {
            data.isEmpty() -> ItemType.NO_DATA.ordinal
            data.size == position -> ItemType.BOTTOM_NAVIGATION.ordinal
            else -> ItemType.ITEM.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemType.ITEM.ordinal -> {
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.itemview_league_v5, parent, false)) //itemview_league_v5
            }
            ItemType.BOTTOM_NAVIGATION.ordinal -> {
                BottomNavigationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.home_bottom_navigation, parent, false))
            }
            else -> {
                NoDataViewHolder.from(parent, searchText)
            }
        }
    }

    // region update by payload functions
    fun updateLeague(position: Int, payload: LeagueOdd) {
        notifyItemChanged(position, payload)
    }

    private fun updateLeagueByBetInfo(position: Int, payload: String) {
        notifyItemChanged(position, payload)
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
            when (payloads.first()) {
                is LeagueOdd -> {
                    val leagueOdd = payloads.first() as LeagueOdd
                    Log.d("Hewie", "更新：聯賽：($position) => ${leagueOdd.league.name}")
                    (holder as ItemViewHolder).update(leagueOdd, matchType, oddsType)
                }
                is String -> {
                    (holder as ItemViewHolder).updateByBetInfo()
                }
            }

        }
    }
    // endregion

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                val item = data[position]
                Log.d("Hewie", "綁定：聯賽：($position) => ${item.league.name}")
                holder.bind(
                    item,
                    matchType,
                    leagueListener,
                    leagueOddListener,
                    oddsType,
                    playSelectedCodeSelectionType,
                    playSelectedCode
                )
            }
        }
    }

    override fun getItemCount(): Int = if (data.isEmpty()) {
        1
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
            playSelectedCodeSelectionType: Int?,
            playSelectedCode: String?
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
            updateLeagueExpand(item, matchType)

        }

        fun updateByBetInfo() {
            updateLeagueOddListByBetInfo()
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

        private fun updateLeagueOddListByBetInfo() {
            leagueOddAdapter.updateByBetInfo(leagueOddListener?.clickOdd)
        }

        private fun updateLeagueExpand(item: LeagueOdd, matchType: MatchType) {
            //itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            //itemView.league_odd_list.visibility = if(item.unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)
        }
        // endregion

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

            //itemView.league_expand.setExpanded(item.unfold == FoldState.UNFOLD.code, false)
            Log.d("Hewie12", "data[adapterPosition].unfold -> ${data[adapterPosition].unfold}")

            expandCheckList[data[adapterPosition].league.id].apply {
                if (this != null) {
                    data[adapterPosition].unfold = if (this == true) FoldState.UNFOLD.code else FoldState.FOLD.code
                }
            }


            itemView.league_odd_list.visibility = if (data[adapterPosition].unfold == FoldState.UNFOLD.code) View.VISIBLE else View.GONE
            updateTimer(matchType, item.gameType)

//            itemView.iv_refresh.isVisible = matchType != MatchType.MY_EVENT

//            itemView.iv_refresh.setOnClickListener {
//                leagueListener?.onRefresh(item)
//            }

            itemView.setOnClickListener {
                if (adapterPosition > data.size - 1) return@setOnClickListener
                data[adapterPosition].unfold = if (data[adapterPosition].unfold == FoldState.UNFOLD.code) {
                    expandCheckList[data[adapterPosition].league.id] = false
                    FoldState.FOLD.code
                } else {
                    expandCheckList[data[adapterPosition].league.id] = true
                    FoldState.UNFOLD.code
                } // TODO IndexOutOfBoundsException: Index: 10, Size: 5
                updateTimer(matchType, item.gameType)

                notifyItemChanged(adapterPosition)

                leagueListener?.onClickLeague(item)
            }
        }

        private fun updateTimer(matchType: MatchType, gameType: GameType?) {
            leagueOddAdapter.isTimerEnable =
                itemView.league_odd_list.visibility == View.VISIBLE && (gameType == GameType.FT || gameType == GameType.BK || gameType == GameType.RB || gameType == GameType.AFT || matchType == MatchType.PARLAY || matchType == MatchType.AT_START || matchType == MatchType.MY_EVENT)
        }
    }

    class NoDataViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        companion object {
            fun from(parent: ViewGroup, searchText: String): NoDataViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val noDataLayoutId = if (searchText.isBlank())
                    R.layout.view_no_record_for_game
                else
                    R.layout.itemview_game_no_record
                val view = layoutInflater
                    .inflate(noDataLayoutId, parent, false)

                return NoDataViewHolder(view)
            }
        }
    }

    class BottomNavigationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

class LeagueListener(
    val clickListenerLeague: (item: LeagueOdd) -> Unit,
    val refreshListener: (item: LeagueOdd) -> Unit
) {
    fun onClickLeague(item: LeagueOdd) = clickListenerLeague(item)
    fun onRefresh(item: LeagueOdd) = refreshListener(item)
}