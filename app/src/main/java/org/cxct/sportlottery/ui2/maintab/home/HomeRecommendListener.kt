package org.cxct.sportlottery.ui2.maintab.home

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd

class HomeRecommendListener(
    private val onItemClickListener: (matchInfo: MatchInfo?) -> Unit,
    private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
    private val onClickPlayTypeListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
) {
    fun onItemClickListener(matchInfo: MatchInfo?) = onItemClickListener.invoke(matchInfo)
    fun onClickBetListener(
        gameType: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?,
    ) = onClickBetListener.invoke(
            gameType,
            matchType,
            matchInfo,
            odd,
            playCateCode,
            playCateName,
            betPlayCateNameMap,
            playCateMenuCode)

    fun onClickPlayTypeListener(
        gameType: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>,
    ) = onClickPlayTypeListener.invoke(gameType, matchType, matchId, matchInfoList)

}