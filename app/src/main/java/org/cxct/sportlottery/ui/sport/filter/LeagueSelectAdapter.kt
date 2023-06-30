package org.cxct.sportlottery.ui.sport.filter

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.outright.odds.MatchOdd

class LeagueSelectAdapter(private val onSelectChanged: (Int) -> Unit) : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(FilterLeagueProvider(this, ::onItemClick)) //联赛
        addNodeProvider(FilterMatchProvider(this, ::onItemClick)) //赔率
    }
    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            else -> 2
        }
    }

    private var itemData = mutableListOf<LeagueOdd>()

    private fun onItemClick(position: Int, view: View, item: BaseNode) {
        if (item is LeagueOdd){
            //联赛选择，更新关联赛事的选中状态
            item.matchOdds.forEach {
                it.isSelected = item.league.isSelected
            }
        } else if(item is org.cxct.sportlottery.network.odds.list.MatchOdd){
            (item.parentNode as LeagueOdd).apply {
                league.isSelected = matchOdds.all { it.isSelected }
            }
        }
        notifyDataSetChanged()
        onSelectChanged.invoke(getSelectSum())
    }

    fun setNewDataList(data: MutableList<LeagueOdd>) {
        itemData = data
        setNewInstance(data as MutableList<BaseNode>)
    }

    fun selectAll(): Int {
        itemData.forEach {
            it.league.isSelected = true
            it.matchOdds.forEach {
                it.isSelected = true
            }
        }
        notifyDataSetChanged()
        return getSelectSum()
    }

    fun reverseSelect(): Int {
        itemData.forEach {
            it.matchOdds.forEach {
                it.isSelected = !it.isSelected
            }
            it.league.isSelected = it.matchOdds.all { it.isSelected }
        }
        notifyDataSetChanged()
        return getSelectSum()
    }

    fun getSelected(): ArrayList<String> {
        var matchSelectList = arrayListOf<String>()
        itemData.forEach {

            if (it.league.isSelected) {
                matchSelectList.addAll(it.matchOdds.map { it.matchInfo?.id?:"" })
            } else {
                it.matchOdds.forEach { matchOdd->
                    if (matchOdd.isSelected) {
                        matchOdd.matchInfo?.id?.let { matchSelectList.add(it) }
                    }
                }
            }
        }

        return matchSelectList
    }

    fun getSelectSum(): Int {
        return itemData.sumOf { it.matchOdds?.count{ it.isSelected } }
    }
}