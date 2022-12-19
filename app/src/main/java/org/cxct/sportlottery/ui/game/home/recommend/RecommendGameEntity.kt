package org.cxct.sportlottery.ui.game.home.recommend

import org.cxct.sportlottery.network.matchCategory.result.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem

class RecommendGameEntity(
    val code: String?, //球種 code
    val name: String?, //球種名稱
    val leagueId: String?,
    val leagueName: String?,
    val matchInfo: MatchInfo?,
    val isOutright: Int?,
    var oddBeans: MutableList<OddBean>,
    val dynamicMarkets: Map<String, DynamicMarket>?,
    val playCateMappingList: List<PlayCateMapItem>?,
    var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?
) {
    var vpRecommendAdapter: VpRecommendAdapter? = null
}

class OddBean(
    val playTypeCode: String,
    val oddList: MutableList<Odd>,
)