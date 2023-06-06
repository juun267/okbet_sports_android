package org.cxct.sportlottery.ui.sport.filter

import android.view.View
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.adapter.BaseNodeAdapter
import org.cxct.sportlottery.network.odds.list.LeagueOdd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds
import org.cxct.sportlottery.network.outright.odds.MatchOdd

class LeagueSelectAdapter(val onItemClick: (Int, View, BaseNode) -> Unit) : BaseNodeAdapter() {

    init {
        addFullSpanNodeProvider(FilterLeagueProvider(this, onItemClick)) //联赛
        addNodeProvider(FilterMatchProvider(this, onItemClick)) //赔率
    }
    override fun getItemType(data: List<BaseNode>, position: Int): Int {
        return when (data[position]) {
            is LeagueOdd -> 1
            else -> 2
        }
    }
}