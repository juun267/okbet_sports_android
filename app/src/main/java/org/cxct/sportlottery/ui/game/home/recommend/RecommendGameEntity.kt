package org.cxct.sportlottery.ui.game.home.recommend

import org.cxct.sportlottery.network.matchCategory.result.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket

class RecommendGameEntity(
    val code: String?, //球種 code
    val name: String?, //球種名稱
    val leagueName: String?,
    val matchInfo: MatchInfo?,
    val isOutright: Int?,
    var oddBeans: List<OddBean>,
    val dynamicMarkets: Map<String, DynamicMarket>?
) {
    var vpRecommendAdapter: VpRecommendAdapter? = null
}

class OddBean(
    val playTypeCode: String,
    val oddList: List<Odd?>,
)