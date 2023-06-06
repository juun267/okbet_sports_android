package org.cxct.sportlottery.ui.sport.list.adapter

import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.list.LeagueOdd

data class SportListGroup(
    val gameType: GameType,
    val number: Int,
    val leagues: MutableList<LeagueOdd>
): BaseNode() {

    override val childNode: MutableList<BaseNode> = leagues as MutableList<BaseNode>

}