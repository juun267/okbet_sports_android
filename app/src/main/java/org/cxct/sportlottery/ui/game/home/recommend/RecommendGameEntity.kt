package org.cxct.sportlottery.ui.game.home.recommend

import org.cxct.sportlottery.network.matchCategory.result.MatchInfo
import org.cxct.sportlottery.network.odds.list.Odd

class RecommendGameEntity(
    val code: String?, //球種 code
    val name: String?, //球種名稱
    val matchInfo: MatchInfo?,
    var oddBeans: List<OddBean>,
) {
    var vpRecommendAdapter: VpRecommendAdapter? = null
}

class OddBean(
    val oddCode: String,
    val oddList: List<Odd>,
)