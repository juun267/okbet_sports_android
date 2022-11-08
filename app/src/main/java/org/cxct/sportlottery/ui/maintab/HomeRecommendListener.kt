package org.cxct.sportlottery.ui.maintab

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.third_game.third_games.hot.HotMatchInfo

class HomeRecommendListener(
    private val onItemClickListener: (matchInfo: MatchInfo?) -> Unit,
    private val onGoHomePageListener: () -> Unit,
    private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
    private val onClickFavoriteListener: (matchId: String?) -> Unit,
    private val onClickStatisticsListener: (matchId: String) -> Unit,
    private val onClickPlayTypeListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    private val onClickLiveIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
    private val onClickAnimationIconListener: (gameType: String, matchType: MatchType?, matchId: String?, matchInfoList: List<MatchInfo>) -> Unit,
) {
    fun onItemClickListener(matchInfo: MatchInfo?,) = onItemClickListener.invoke(matchInfo)
    fun onClickBetListener(
        gameType: String,
        matchType: MatchType,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String,
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
        playCateMenuCode: String?,
    ) {
        onClickBetListener.invoke(
            gameType,
            matchType,
            matchInfo,
            odd,
            playCateCode,
            playCateName,
            betPlayCateNameMap,
            playCateMenuCode
        )
    }

    fun onClickFavoriteListener(matchId: String?) = onClickFavoriteListener.invoke(matchId)
    fun onClickStatisticsListener(matchId: String) = onClickStatisticsListener.invoke(matchId)
    fun onClickPlayTypeListener(
        gameType: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>,
    ) =
        onClickPlayTypeListener.invoke(gameType, matchType, matchId, matchInfoList)

    fun onClickLiveIconListener(
        gameType: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>,
    ) = onClickLiveIconListener.invoke(gameType, matchType, matchId, matchInfoList)

    fun onClickAnimationIconListener(
        gameType: String,
        matchType: MatchType?,
        matchId: String?,
        matchInfoList: List<MatchInfo>,
    ) = onClickAnimationIconListener.invoke(gameType, matchType, matchId, matchInfoList)
}