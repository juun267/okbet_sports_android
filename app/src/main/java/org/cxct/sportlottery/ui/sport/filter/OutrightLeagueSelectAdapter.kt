package org.cxct.sportlottery.ui.sport.filter

import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.provider.BaseNodeProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.github.promeg.pinyinhelper.Pinyin
import org.cxct.sportlottery.R
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.outright.odds.MatchOdd
import org.cxct.sportlottery.network.outright.odds.LeagueOdd
import org.cxct.sportlottery.util.VerifyConstUtil
import org.cxct.sportlottery.util.setArrowSpin
import org.cxct.sportlottery.util.setExpandArrow
import org.cxct.sportlottery.util.setLeagueLogo

class OutrightLeagueSelectAdapter(private val onSelectChanged: (Int) -> Unit) : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(FilterOutrightLeagueProvider(this, ::onItemClick)) //联赛
        addNodeProvider(FilterOutrightMatchProvider(this, ::onItemClick)) //赔率
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
            item.matchOdds!!.forEach {
                it.isSelected = item.league!!.isSelected
            }
        } else if(item is org.cxct.sportlottery.network.odds.list.MatchOdd){
            (item.parentNode as org.cxct.sportlottery.network.odds.list.LeagueOdd).apply {
                league.isSelected = matchOdds.all { it.isSelected }
            }
        }
        notifyDataSetChanged()
        onSelectChanged.invoke(getSelectSum())
    }

    fun setNewDataList(leagues: List<LeagueOdd>): Map<String, List<LeagueOdd>> {
        val resultList = mutableListOf<LeagueOdd>()
        leagues.forEach { leagueOdd->
            if (leagueOdd.league != null && !leagueOdd.matchOdds.isNullOrEmpty()) { // 过滤无效数据
                leagueOdd.matchOdds.forEach {
                    it.parentNode = leagueOdd
                    it.oddsMap?.clear()  // 清除三级节点玩法数据
                }
                val firstCap = Pinyin.toPinyin(leagueOdd.league.name.first()).first().toString()
                leagueOdd.league.firstCap = if(VerifyConstUtil.isValidEnglishWord(firstCap)) firstCap else "#"
                resultList.add(leagueOdd)
            }
        }

        var map: Map<String, List<LeagueOdd>> = resultList.groupBy {
            it.league!!.firstCap
        }.toSortedMap { o1: String, o2: String ->
            o1.compareTo(o2)
        }

        val sortedListData = mutableListOf<LeagueOdd>()
        map.keys.forEach { name ->
            sortedListData.addAll(map[name] ?: mutableListOf())
        }
        itemData = sortedListData
        setNewInstance(sortedListData as MutableList<BaseNode>)
        return map
    }

    fun selectAll(): Int {
        itemData.forEach {
            it.league!!.isSelected = true
            it.matchOdds!!.forEach {
                it.isSelected = true
            }
        }
        notifyDataSetChanged()
        return getSelectSum()
    }

    fun reverseSelect(): Int {
        itemData.forEach {
            it.matchOdds!!.forEach {
                it.isSelected = !it.isSelected
            }
            it.league!!.isSelected = it.matchOdds.all { it.isSelected }
        }
        notifyDataSetChanged()
        return getSelectSum()
    }

    fun getSelectedMatchIds(): ArrayList<String> {
        var matchSelectList = arrayListOf<String>()
        itemData.forEach {

            if (it.league!!.isSelected) {
                matchSelectList.addAll(it.matchOdds!!.map { it.matchInfo?.id?:"" })
            } else {
                it.matchOdds!!.forEach { matchOdd->
                    if (matchOdd.isSelected) {
                        matchOdd.matchInfo?.id?.let { matchSelectList.add(it) }
                    }
                }
            }
        }

        return matchSelectList
    }
    fun getSelectedLeagueIds(): ArrayList<String> {
        var matchSelectList = arrayListOf<String>()
        matchSelectList.addAll(itemData.filter { it.league!!.isSelected }.map {it.league!!.id })
        return matchSelectList
    }

    fun getSelectSum(): Int {
        return itemData.sumOf { it.matchOdds?.count{ it.isSelected } ?: 0 }
    }
}

class FilterOutrightLeagueProvider(val adapter: OutrightLeagueSelectAdapter,
                           val onItemClick:(Int, View, BaseNode) -> Unit,
                           override val itemViewType: Int = 1,
                           override val layoutId: Int = R.layout.item_filter_league): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode)  {
        val leagueOdd = item as LeagueOdd
        val league = leagueOdd.league!! // 已经提前过滤掉数据为空的情况
        val matchOdds = leagueOdd.matchOdds!! // 已经提前过滤掉数据为空的情况
        helper.setText(R.id.tvLeagueName, league.name)
        helper.getView<ImageView>(R.id.ivLogo).setLeagueLogo(leagueOdd.league.categoryIcon)
        helper.setText(R.id.tvNum, matchOdds.count { it.isSelected }.toString())
        helper.setText(R.id.tvTotalNum,matchOdds.size.toString())
        val ivArrow = helper.getView<ImageView>(R.id.ivArrow)
        helper.itemView.isSelected = leagueOdd.isExpanded
        val ivCheck=helper.getView<ImageView>(R.id.ivCheck)
        ivCheck.isSelected = league.isSelected
        setExpandArrow(ivArrow, leagueOdd.isExpanded)
        ivArrow.setOnClickListener {
            adapter.expandOrCollapse(item)
            helper.itemView.isSelected = leagueOdd.isExpanded
            ivArrow.setArrowSpin(leagueOdd.isExpanded, true) { setExpandArrow(ivArrow, leagueOdd.isExpanded) }
        }
        helper.itemView.setOnClickListener {
            league.isSelected = !league.isSelected
            ivCheck.isSelected = league.isSelected
            onItemClick.invoke(adapter.getItemPosition(item), it, leagueOdd)
        }
    }

}


class FilterOutrightMatchProvider(val adapter: OutrightLeagueSelectAdapter,
                          val onItemClick:(Int, View, BaseNode) -> Unit,
                          override val itemViewType: Int = 2,
                          override val layoutId: Int = R.layout.item_filter_match): BaseNodeProvider() {

    override fun convert(helper: BaseViewHolder, item: BaseNode) {
        val matchOdd = item as MatchOdd
        helper.setText(R.id.tvMatchName, matchOdd.matchInfo?.name)
        val ivCheck=helper.getView<ImageView>(R.id.ivCheck)
        ivCheck.isSelected = matchOdd.isSelected
        helper.itemView.setOnClickListener {
            matchOdd.isSelected = !matchOdd.isSelected
            ivCheck.isSelected = matchOdd.isSelected
            onItemClick.invoke(adapter.getItemPosition(item), it, matchOdd)
        }
    }

    override fun onClick(helper: BaseViewHolder, view: View, data: BaseNode, position: Int) {
        onItemClick.invoke(position, view, data)
    }

}