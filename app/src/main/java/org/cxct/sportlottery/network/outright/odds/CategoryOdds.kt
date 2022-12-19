package org.cxct.sportlottery.network.outright.odds

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.network.odds.Odd

data class CategoryOdds(val name: String,
                        val matchOdd: MatchOdd,
                        val playCate: String,
                        val oddList: MutableList<Odd>): BaseExpandNode() {
    init {
        oddList.forEach { it.parentNode = this }
    }

    override val childNode: MutableList<BaseNode>? = oddList as MutableList<BaseNode>

    fun indexOf(odd: Odd): Int {
        return oddList.indexOf(odd)
    }
}