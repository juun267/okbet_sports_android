package org.cxct.sportlottery.ui.sport.list.adapter//package org.cxct.sportlottery.ui.sport

import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.common.QuickPlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.util.QuickListManager
import org.cxct.sportlottery.util.SocketUpdateUtil
import org.cxct.sportlottery.util.SocketUpdateUtil.toMutableFormat_1
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus

class SportLeagueAdapter2(
    var matchType: MatchType,
    val lifecycleOwner: LifecycleOwner,
    onOddClick: OnOddClickListener,
    onFavorite:(String) -> Unit

): ExpanableOddsAdapter() {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        addFullSpanNodeProvider(SportLeagueProvider(this)) // 联赛名
        addFullSpanNodeProvider(SportMatchProvider(this, onOddClick, onFavorite)) // 赛事
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            is MatchOdd -> 2
            else -> 3
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is SportMatchVH) {
            holder.onStop()
        }
    }

    private fun updateMatch(index: Int, matchOdd: MatchOdd) {
//        if (game_list.scrollState == RecyclerView.SCROLL_STATE_IDLE && !game_list.isComputingLayout) {
//            sportLeagueAdapter.updateMatch(index, matchOdd)
//        }
    }

    fun onOddsChangeEvent(oddsChangeEvent: OddsChangeEvent, subscribedMatchOdd: MutableMap<String, Pair<MatchOdd, Int>>) {
        if (oddsChangeEvent.oddsList.isNullOrEmpty() || subscribedMatchOdd.isEmpty()) {
            return
        }

        val matchOddWithPosition = subscribedMatchOdd[oddsChangeEvent.eventId] ?: return

        if (updateMatchOdds(matchOddWithPosition.first, oddsChangeEvent)) {
            notifyItemChanged(matchOddWithPosition.second, matchOddWithPosition.first)
//            updateMatch(leagueIndex, matchOdd)
//            updateBetInfo(leagueOdd, oddsChangeEvent)
        }
    }

    private fun updateMatchOdds(matchOdd: org.cxct.sportlottery.network.common.MatchOdd,
                                oddsChangeEvent: OddsChangeEvent): Boolean {

        var isNeedRefresh = false
        var isNeedRefreshPlayCate = false

        val cateMenuCode = oddsChangeEvent.channel?.split(context.getString(R.string.splash_no_trans))?.getOrNull(6)

        isNeedRefresh = when {

            (cateMenuCode == PlayCate.EPS.value) -> {
                SocketUpdateUtil.updateMatchOdds(
                    mutableMapOf(Pair(PlayCate.EPS.value, matchOdd.oddsEps?.eps?.toMutableList() ?: mutableListOf())),
                    oddsChangeEvent.odds)
            }


            (QuickPlayCate.values().map { it.value }.contains(cateMenuCode)) -> {
                SocketUpdateUtil.updateMatchOdds(
                    matchOdd.quickPlayCateList?.find { it.isSelected }?.quickOdds?.toMutableFormat_1()
                        ?: mutableMapOf(), oddsChangeEvent.odds)
            }

            else -> {
                if (matchOdd.oddsMap == null) {
                    matchOdd.oddsMap = mutableMapOf()
                }
                SocketUpdateUtil.updateMatchOdds(
                    matchOdd.oddsMap as MutableMap<String, MutableList<Odd?>?>? ?: mutableMapOf(),
                    oddsChangeEvent.odds
                )
            }
        }

        //更新翻譯
        if (matchOdd.betPlayCateNameMap == null) {
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }

        SocketUpdateUtil.updateBetPlayCateNameMap(
            matchOdd.betPlayCateNameMap,
            oddsChangeEvent.betPlayCateNameMap
        )

        if (matchOdd.playCateNameMap == null) {
            matchOdd.playCateNameMap = mutableMapOf()
        }

        SocketUpdateUtil.updatePlayCateNameMap(
            matchOdd.playCateNameMap,
            oddsChangeEvent.playCateNameMap
        )

        isNeedRefreshPlayCate = when (matchOdd.quickPlayCateList.isNullOrEmpty()) {
            true -> {
                SocketUpdateUtil.insertPlayCate(matchOdd, oddsChangeEvent, matchType)
            }

            false -> {
                SocketUpdateUtil.refreshPlayCate(matchOdd, oddsChangeEvent, matchType)
            }
        }

        isNeedRefreshPlayCate =
            isNeedRefreshPlayCate || (matchOdd.matchInfo?.playCateNum != oddsChangeEvent.playCateNum)

        if (isNeedRefresh) {
            SocketUpdateUtil.sortOdds(matchOdd)
            matchOdd.updateOddStatus()
        }

        if (isNeedRefreshPlayCate) {
            matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum
        }

        return isNeedRefresh || isNeedRefreshPlayCate

    }

    fun removeMatchOdd(matchOdd: MatchOdd) {
        remove(matchOdd)
        rootNodes
    }

    fun updateOddsSelectStatus(matchOdds: Collection<Pair<MatchOdd, Int>>) {
        matchOdds.forEach { matchOddPosition ->
            val matchOdd = matchOddPosition.first
            matchOdd.oddsMap?.values?.forEachIndexed { _, oddsList ->
                oddsList?.forEach { odd ->
                    val isSelected = odd.id?.let { QuickListManager.containOdd(it) } == true
                    if (odd.isSelected != isSelected) {
                        odd.isSelected = isSelected
                        notifyItemChanged(matchOddPosition.second, matchOdd)
                        return@forEachIndexed
                    }
                }
            }
        }
    }


}