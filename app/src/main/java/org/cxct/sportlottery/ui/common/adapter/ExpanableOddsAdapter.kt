package org.cxct.sportlottery.ui.common.adapter

import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.ui.betList.BetInfoListData

// 可展开收起的体育赛事赔率列表
abstract class ExpanableOddsAdapter<T: MatchOdd>: BaseNodeAdapter() {

    fun getCount() = getDefItemCount()

    var rootNodes: MutableList<BaseNode>? = null // 一级父节点集合

    override fun setNewInstance(list: MutableList<BaseNode>?) {
        currentVisiableMatchOdds.clear()
        rootNodes = list
        super.setNewInstance(list)
    }

    //展开全部
    fun expandAll() {
        rootNodes?.forEach {
            val position = getItemPosition(it)
            if (position >= 0) {
                expand(position, parentPayload = it)
            }
        }
    }

    // 收起全部
    fun collapseAll() {
        rootNodes?.forEach {
            val position = getItemPosition(it)
            if (position >= 0) {
                collapse(position, parentPayload = it)
            }
        }
    }

    // 根据注单更新选中状态
    fun updateOddsSelectedStatus(betInfoList: List<BetInfoListData>) {
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
            val item = getItemOrNull(i)
            if(item is Odd) {
                val isSelected = betInfoMap.containsKey(item.id)
                if (isSelected != item.isSelected) {
                    item.isSelected = isSelected
                    notifyItemChanged(i)
                }
            }
        }
    }

    fun refreshOdds(oldOdds: Odd, newOdds: Odd) {
        val oddSocketValue = newOdds.odds
        if (oddSocketValue != null) {
            oldOdds.updateOdd(oddSocketValue)
        }

        oldOdds.hkOdds = newOdds.hkOdds
        oldOdds.malayOdds = newOdds.malayOdds
        oldOdds.indoOdds = newOdds.indoOdds

        //更新是不是只有歐洲盤 (因為棒球socket有機會一開始全部賠率推0.0)，跟後端(How)確認過目前只有棒球會這樣。
        oldOdds.isOnlyEUType = newOdds.odds == newOdds.hkOdds && newOdds.odds == newOdds.malayOdds && newOdds.odds == newOdds.indoOdds

        if (oldOdds.status != newOdds.status) {
            oldOdds.status = newOdds.status
        }

        if (oldOdds.spread != newOdds.spread) {
            oldOdds.spread = newOdds.spread
        }

        if (oldOdds.extInfo != newOdds.extInfo) {
            oldOdds.extInfo = newOdds?.extInfo
        }
        if (oldOdds.version != newOdds.version) {
            oldOdds.version = newOdds?.version
        }
    }

    // 可见区域的MatchOdd
    protected val currentVisiableMatchOdds = mutableMapOf<String, T>()

    // 滑动改版后，标记当前可见区域的MatchOdd(后面socket更新知对该区域操作，重新绑定数据或者滑动改变后重新标记该区域)
    fun recodeRangeMatchOdd(): MutableList<T> {

        currentVisiableMatchOdds.clear()
        val matchOdds = mutableListOf<T>()
        recyclerView.children.forEach {
            if (it.tag is MatchOdd) {
                val matchOdd = it.tag as T
                matchOdds.add(matchOdd)
                matchOdd.matchInfo?.let { currentVisiableMatchOdds[it.id] = matchOdd }
            }
        }

        return matchOdds
    }

    fun findVisiableRangeMatchOdd(id: String): T? {
        return currentVisiableMatchOdds[id]
    }

    fun resetRangeMatchOdd() {
        currentVisiableMatchOdds.clear()
    }

}