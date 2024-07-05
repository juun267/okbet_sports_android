package org.cxct.sportlottery.ui.sport.outright

import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.common.adapter.ExpanableOddsAdapter
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus

class SportOutrightAdapter2(val lifecycle: LifecycleOwner, val onItemClick:(Int, View, BaseNode) -> Unit): ExpanableOddsAdapter<MatchOdd>() {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        footerWithEmptyEnable = true
        addFullSpanNodeProvider(OutrightFirstProvider(this, lifecycle, onItemClick)) //联赛
        addFullSpanNodeProvider(OutrightSecondProvider(this, lifecycle, onItemClick)) //获胜名称
        addNodeProvider(OutrightThirdProvider(this, lifecycle, onItemClick)) //赔率
    }

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is MatchOdd -> 1
            is CategoryOdds -> 2
            else -> 3
        }
    }

    private fun insertSecondList(matchOdd: MatchOdd, playCate: String, name: String, oddList: MutableList<Odd>) {
        matchOdd.oddsMap!![playCate] = oddList
        val categoryOdds = CategoryOdds(name, matchOdd, playCate, oddList)
        printLog("插入一级列表下新增二级列表 ${matchOdd.matchInfo?.name}  $name")
        nodeAddData(matchOdd, mutableListOf<BaseNode>(categoryOdds))
    }

    // scoket推送的赔率变化，更新列表
    fun onMatchOdds(oddsChangeEvent: OddsChangeEvent): Boolean {
        if (oddsChangeEvent.eventId.isEmptyStr() || oddsChangeEvent.odds.isNullOrEmpty()) {
            return false
        }

        val matchOdd = currentVisiableMatchOdds[oddsChangeEvent.eventId] ?: return false

        if (oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }

        var needNotifyAllChanged = false

        matchOdd.oddsMap?.forEach { (k, v) ->
            matchOdd.oddIdsMap.clear()

            val idsMap = mutableMapOf<String, Odd>()
            v?.forEach { odd -> odd?.id?.let { idsMap[it] = odd } }

            matchOdd.oddIdsMap[k] = idsMap
        }

        if (updateMatchOdds(matchOdd, oddsChangeEvent)) {
            needNotifyAllChanged = true
        }

        if (needNotifyAllChanged) {
            notifyDataSetChanged()
        }

        return needNotifyAllChanged
    }

    // 赔率变化更新列表赔率
    private fun updateMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent, matchType: MatchType? = null): Boolean {

        var isNeedRefresh = false

        var oddsMap = matchOdd.oddsMap
        if (oddsChangeEvent.updateMode == 2){
            matchOdd.oddsMap = oddsChangeEvent.odds
            isNeedRefresh = true
        }else if (oddsMap.isNullOrEmpty()) {
            if(oddsMap == null){
                oddsMap = mutableMapOf()
                matchOdd.oddsMap = oddsMap
            }

//            // 如果一级列表下面为空，就向该一级列表下插入二级列表
//            // 返回的数据有不属于该联赛玩法的赔率，不进行新增显示
//            for ((key, value) in oddsChangeEvent.odds.entries) {
//                if (value.isNullOrEmpty()) {
//                    continue
//                }
//
//                insertSecondList(matchOdd, key, oddsChangeEvent.dynamicMarkets?.get(key)?.get() ?: "", value)
//            }
        } else {

            for ((key, value) in oddsChangeEvent.odds.entries) {
                if (value.isNullOrEmpty()) {
                    continue
                }

                if (oddsMap.containsKey(key)) {
                    var oddList = oddsMap[key]
                    if (oddList == null) { // 二级节点下面增加三级节点列表
                        oddsMap[key] = value
                        nodeReplaceChildData(matchOdd.categoryOddsMap[key]!!, value)
                    } else {
                        isNeedRefresh = updateMatchOdds(key, matchOdd, oddList, value, oddsChangeEvent.updateMode)
                    }
                } else { // 一级节点下面增加二级节点
                    // 返回的数据有不属于该联赛玩法的赔率，不进行新增显示
//                    insertSecondList(matchOdd, key, oddsChangeEvent.dynamicMarkets?.get(key)?.get() ?: "", value)
                }
            }
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

    private fun updateMatchOdds(playCate: String, matchOdd: MatchOdd, oddsMap: MutableList<Odd>, oddsMapSocket: List<Odd>, updateMode: Int?): Boolean {

        if (oddsMap.isEmpty()) {
            nodeReplaceChildData(matchOdd.categoryOddsMap[playCate]!!, oddsMapSocket!!)
            return false
        }

        return refreshMatchOdds(playCate, matchOdd, oddsMap, oddsMapSocket!!)
    }


    private fun refreshMatchOdds(playCate: String, matchOdd: MatchOdd, oddsMap: MutableList<Odd>, oddsMapSocket: List<Odd>): Boolean {

        if (oddsMap.all { it == null }) { // 如果oddsMap里面全是空对象(之前的逻辑)
            nodeReplaceChildData(matchOdd.categoryOddsMap[playCate]!!, oddsMapSocket)
            return false
        }

        val oddIdsMap = matchOdd.oddIdsMap[playCate] ?: return false

        var isNeedRefresh = false
        for (socketOdd in oddsMapSocket) {
            if (socketOdd == null) {
                continue
            }

            val odd = oddIdsMap[socketOdd.id]
            if (odd == null) {
                oddsMap.add(socketOdd)
                continue
            }

            isNeedRefresh = true
            refreshOdds(odd, socketOdd)
        }

        return isNeedRefresh
    }

    private fun printLog(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d("SportOutrightAdapter2", "=====>>> $msg")
        }
    }

}


