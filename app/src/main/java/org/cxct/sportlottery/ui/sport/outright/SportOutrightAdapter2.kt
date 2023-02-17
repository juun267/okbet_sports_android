package org.cxct.sportlottery.ui.sport.outright

import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.BuildConfig
import org.cxct.sportlottery.adapter.recyclerview.BaseNodeAdapter
import org.cxct.sportlottery.enum.OddState
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.service.odds_change.OddsChangeEvent
import org.cxct.sportlottery.ui.bet.list.BetInfoListData
import org.cxct.sportlottery.ui.menu.OddsType
import org.cxct.sportlottery.util.SocketUpdateUtil.updateOddStatus

class SportOutrightAdapter2(val lifecycle: LifecycleOwner, val onItemClick:(Int, View, BaseNode) -> Unit): BaseNodeAdapter() {

    var oddsType: OddsType = OddsType.EU
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        addFullSpanNodeProvider(OutrightFirstProvider(this, lifecycle, onItemClick)) //联赛
        addFullSpanNodeProvider(OutrightSecondProvider(this, lifecycle, onItemClick)) //获胜名称
        addNodeProvider(OutrightThirdProvider(this, lifecycle, onItemClick)) //赔率
    }

    fun getCount() = getDefItemCount()

    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is MatchOdd -> 1
            is CategoryOdds -> 2
            else -> 3
        }
    }

    var rootNodes: MutableList<BaseNode>? = null // 一级父节点集合

    override fun setNewInstance(list: MutableList<BaseNode>?) {
        rootNodes = list
        super.setNewInstance(list)

    }

    //展开全部
    fun expandAll() {
        rootNodes?.forEach {
            val position = getItemPosition(it)
            if (position >= 0) {
                expand(position)
            }
        }
    }

    // 收起全部
    fun collapseAll() {
        rootNodes?.forEach {
            val position = getItemPosition(it)
            if (position >= 0) {
                collapse(position)
            }
        }
    }

    // 根据注单更新选中状态
    fun updateOddsSelectedStatus(betInfoList: MutableList<BetInfoListData>) {
        if (getDefItemCount() == 0) {
            return
        }

        val layoutManager = recyclerViewOrNull?.layoutManager as LinearLayoutManager? ?: return
        val first = layoutManager.findFirstVisibleItemPosition()
        if (first < 0) {
            return
        }

        val last = layoutManager.findLastVisibleItemPosition()
        if (last < 0) {
            return
        }

        val betInfoMap = mutableMapOf<String, BetInfoListData>()
        betInfoList.forEach { betInfoMap.put(it.matchOdd.oddsId, it) }


        for (i in first..last) {
            val item = getItem(i)
            if(item is Odd) {
                val isSelected = betInfoMap.containsKey(item.id)
                if (isSelected != (item.isSelected == true)) {
                    item.isSelected = isSelected
                    notifyItemChanged(i)
                }
            }
        }
    }

    private fun insertSecondList(matchOdd: MatchOdd, playCate: String, name: String, oddList: MutableList<Odd>) {
        matchOdd.oddsMap!![playCate] = oddList
        val categoryOdds = CategoryOdds(name, matchOdd, playCate, oddList)
        printLog("插入一级列表下新增二级列表 ${matchOdd.matchInfo?.name}  $name")
        nodeAddData(matchOdd, mutableListOf<BaseNode>(categoryOdds))
    }

    // scoket推送的赔率变化，更新列表
    fun onMatchOdds(subscribedMatchOddList: MutableList<MatchOdd>, oddsChangeEvent: OddsChangeEvent): Boolean {
        var needNotifyAllChanged = false
        subscribedMatchOddList.forEach { mOdds ->

            mOdds.oddsMap?.forEach { (k, v) ->
                mOdds.oddIdsMap.clear()

                val idsMap = mutableMapOf<String, Odd>()
                v?.forEach { odd -> odd?.id?.let { idsMap[it] = odd } }

                mOdds.oddIdsMap[k] = idsMap
            }

            if (updateMatchOdds(mOdds, oddsChangeEvent)) {
                needNotifyAllChanged = true
            }
        }

        if (needNotifyAllChanged) {
            notifyDataSetChanged()
        }

        return needNotifyAllChanged
    }

    // 赔率变化更新列表赔率
    private fun updateMatchOdds(matchOdd: MatchOdd, oddsChangeEvent: OddsChangeEvent, matchType: MatchType? = null): Boolean {

        if (oddsChangeEvent.eventId == null
            || oddsChangeEvent.odds.isNullOrEmpty()
            || oddsChangeEvent.eventId != matchOdd.matchInfo?.id
            || oddsChangeEvent.channel?.split("/")?.getOrNull(6) != matchOdd.matchInfo?.id) {
            return false
        }

        var isNeedRefresh = false

        var oddsMap = matchOdd.oddsMap
        if (oddsMap.isNullOrEmpty()) {
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
                        oddsMap[key] = oddList
                        nodeReplaceChildData(matchOdd.categoryOddsMap[key]!!, value)
                    } else {
                        isNeedRefresh = updateMatchOdds(key, matchOdd, oddList, value)
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

    private fun updateMatchOdds(playCate: String, matchOdd: MatchOdd, oddsMap: MutableList<Odd>, oddsMapSocket: List<Odd>): Boolean {

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
            val oddValue = odd.odds
            val oddSocketValue = socketOdd.odds
           if (oddValue != null && oddSocketValue != null) {
                when {
                    oddValue > oddSocketValue -> {
                        odd.oddState = OddState.SMALLER.state
                    }
                    oddValue < oddSocketValue -> {
                        odd.oddState = OddState.LARGER.state
                    }
                    oddValue == oddSocketValue -> {
                        odd.oddState = OddState.SAME.state
                    }
                }
            }

            odd.odds = socketOdd.odds
            odd.hkOdds = socketOdd.hkOdds
            odd.malayOdds = socketOdd.malayOdds
            odd.indoOdds = socketOdd.indoOdds

            //更新是不是只有歐洲盤 (因為棒球socket有機會一開始全部賠率推0.0)，跟後端(How)確認過目前只有棒球會這樣。
            odd.isOnlyEUType = socketOdd.odds == socketOdd.hkOdds && socketOdd.odds == socketOdd.malayOdds && socketOdd.odds == socketOdd.indoOdds

            if (odd.status != socketOdd.status) {
                odd.status = socketOdd.status
            }

            if (odd.spread != socketOdd.spread) {
                odd.spread = socketOdd.spread
            }

            if (odd.extInfo != socketOdd.extInfo) {
                odd.extInfo = socketOdd?.extInfo
            }
        }

        return isNeedRefresh
    }

    private fun printLog(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d("SportOutrightAdapter2", "=====>>> $msg")
        }
    }

}


