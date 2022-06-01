package org.cxct.sportlottery.ui.game.common

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

/**
 * @author kevin
 * @create 2022/4/19
 * @description
 */
class LeagueOddListener(
    /**
     * matchType 專給串關使用, 主要辨別是否為滾球, 從串關跳轉至滾球賽事詳情
     */
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
    val clickListenerBet: (matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) -> Unit,
    val clickListenerQuickCateTab: (matchOdd: MatchOdd, quickPlayCate: QuickPlayCate) -> Unit,
    val clickListenerQuickCateClose: () -> Unit,
    val clickListenerFavorite: (matchId: String?) -> Unit,
    val clickListenerStatistics: (matchId: String?) -> Unit,
    val refreshListener: (leagueId: String) -> Unit,
    val clickLiveIconListener: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
    val clickAnimationIconListener: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
) {
    fun onClickPlayType(matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int = 0) =
        clickListenerPlayType(matchId, matchInfoList, gameMatchType, liveVideo)

    var clickOdd: Odd? = null

    fun onClickBet(
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String = "",
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        clickOdd = odd
        clickListenerBet(matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)
    }

    fun onClickQuickCateTab(matchOdd: MatchOdd, quickPlayCate: QuickPlayCate) = clickListenerQuickCateTab(matchOdd, quickPlayCate)

    fun onClickQuickCateClose() = clickListenerQuickCateClose()

    fun onClickFavorite(matchId: String?) = clickListenerFavorite(matchId)

    fun onClickStatistics(matchId: String?) = clickListenerStatistics(matchId)

    fun onRefresh(leagueId: String) = refreshListener(leagueId)

    fun onClickLiveIconListener(
        matchId: String?,
        matchInfoList: List<MatchInfo>,
        gameMatchType: MatchType,
        liveVideo: Int
    ) = clickLiveIconListener(matchId, matchInfoList, gameMatchType, liveVideo)

    fun onClickAnimationIconListener(
        matchId: String?,
        matchInfoList: List<MatchInfo>,
        gameMatchType: MatchType,
        liveVideo: Int
    ) = clickAnimationIconListener(matchId, matchInfoList, gameMatchType, liveVideo)
}