package org.cxct.sportlottery.ui.sport.endscore

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.extentions.isEmptyStr
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.ExpanableOddsAdapter
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus

// 篮球末位比分
class EndScoreAdapter(val playCate: String, val onItemClick:(Int, View, BaseNode) -> Unit): ExpanableOddsAdapter()  {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        addFullSpanNodeProvider(EndScoreFirstProvider(this, onItemClick)) // 联赛
        addFullSpanNodeProvider(EndScoreSecondProvider(this, onItemClick)) // 比赛球队
        addNodeProvider(EndScoreThirdProvider(this, onItemClick)) //赔率
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            is MatchOdd -> 2
            else -> 3
        }
    }


    // scoket推送的赔率变化，更新列表
    fun onMatchOdds(subscribedMatchOddList: MutableMap<String, MatchOdd>, oddsChangeEvent: OddsChangeEvent): Boolean {
        if (oddsChangeEvent.eventId.isEmptyStr() || oddsChangeEvent.odds.isNullOrEmpty()) {
            return false
        }

        val matchOdd = subscribedMatchOddList[oddsChangeEvent.eventId] ?: return false
        if (oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }

        // 过滤玩法赔率
        val newOdds = oddsChangeEvent.odds[playCate]
        if (newOdds.isNullOrEmpty()) {
            return false
        }

        var needNotifyAllChanged = false
        if (updateMatchOdds(matchOdd, newOdds, oddsChangeEvent)) {
            needNotifyAllChanged = true
        }

        if (needNotifyAllChanged) {
            notifyDataSetChanged()
        }

        return needNotifyAllChanged
    }

    private fun resetOddIdsMap(matchOdd: MatchOdd, playCate: String) {
        val oddList = matchOdd.childNode as MutableList<Odd>
        val idsMap = mutableMapOf<String, Odd>()
        oddList.forEach { odd->
            odd.parentNode = matchOdd
            odd?.id?.let { idsMap[it] = odd }
            matchOdd.oddIdsMap[playCate] = idsMap
        }
    }

    // 赔率变化更新列表赔率
    private fun updateMatchOdds(matchOdd: MatchOdd, newOdds: MutableList<Odd>, oddsChangeEvent: OddsChangeEvent): Boolean {

        var isNeedRefresh = false
        var oddsMap = matchOdd.childNode as MutableList<Odd>
        if (oddsMap.isNullOrEmpty()) {
            nodeAddData(matchOdd, newOdds)
            resetOddIdsMap(matchOdd, playCate)
        } else {
            val oddList = matchOdd.childNode as MutableList<Odd>
            isNeedRefresh = refreshMatchOdds(playCate, matchOdd, oddList, newOdds)
        }

        //更新翻譯
        if(matchOdd.betPlayCateNameMap == null){
            matchOdd.betPlayCateNameMap = mutableMapOf()
        }
        oddsChangeEvent.betPlayCateNameMap?.let { matchOdd.betPlayCateNameMap!!.putAll(it) }

        if(matchOdd.playCateNameMap == null){
            matchOdd.playCateNameMap = mutableMapOf()
        }

        oddsChangeEvent.playCateNameMap?.let { matchOdd.playCateNameMap!!.putAll(it) }

        if (isNeedRefresh) {
//            SocketUpdateUtil.sortOdds(matchOdd)
            matchOdd.updateOddStatus()
        }

        matchOdd.matchInfo?.playCateNum = oddsChangeEvent.playCateNum

        if (isNeedRefresh) {
            notifyDataSetChanged()
        }

        return isNeedRefresh
    }

    private fun refreshMatchOdds(playCate: String, matchOdd: MatchOdd, oddsMap: MutableList<Odd>, oddsMapSocket: List<Odd>): Boolean {

        if (oddsMap.all { it == null }) { // 如果oddsMap里面全是空对象(之前的逻辑)
            nodeReplaceChildData(matchOdd, oddsMapSocket)
            resetOddIdsMap(matchOdd, playCate)
            return false
        }

        val oddIdsMap: MutableMap<String, Odd> = matchOdd.oddIdsMap[playCate] ?: return false

        var isNeedRefresh = false
        for (socketOdd in oddsMapSocket) {
            if (socketOdd == null) {
                continue
            }

            val odd = oddIdsMap[socketOdd.id]
            if (odd == null) {
                socketOdd.parentNode = matchOdd
                oddIdsMap["${socketOdd.id}"] = socketOdd
                oddsMap.add(socketOdd)
                continue
            }

            isNeedRefresh = true
            refreshOdds(odd, socketOdd)
        }

        return isNeedRefresh
    }

}