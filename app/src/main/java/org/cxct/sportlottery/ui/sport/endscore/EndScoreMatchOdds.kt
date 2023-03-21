package org.cxct.sportlottery.ui.sport.endscore

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd

class EndScoreMatchOdds(val name: String,
                        val matchOdd: MatchOdd,
                        val playCate: String,
                        val oddList: MutableList<Odd>): BaseExpandNode() {
//    init {
//        matchOdd.categoryOddsMap[playCate] = this
//        oddList.forEach { it.parentNode = this }
//    }

    override val childNode: MutableList<BaseNode>? = oddList as MutableList<BaseNode>

    fun indexOf(odd: Odd): Int {
        return oddList.indexOf(odd)
    }
}