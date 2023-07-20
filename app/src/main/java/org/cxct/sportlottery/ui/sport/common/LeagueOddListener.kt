package org.cxct.sportlottery.ui.sport.common

import android.view.View
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

/**
 * @author kevin
 * @create 2022/4/19
 * @description
 */
@Deprecated("回调方法分开写，在使用的时候不需要的方法就不用关注了")
class LeagueOddListener(
    /**
     * matchType 專給串關使用, 主要辨別是否為滾球, 從串關跳轉至滾球賽事詳情
     */
    val clickListenerPlayType: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
    val clickListenerBet: (view: View, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?) -> Unit,
    val clickListenerFavorite: (matchId: String?) -> Unit,
    val clickListenerStatistics: (matchId: String?) -> Unit,
    val clickLiveIconListener: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
    val clickAnimationIconListener: (matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int) -> Unit,
    val clickCsTabListener: (playCate: PlayCate, matchOdd: MatchOdd) -> Unit
) {
    fun onClickPlayType(matchId: String?, matchInfoList: List<MatchInfo>, gameMatchType: MatchType, liveVideo: Int = 0) =
        clickListenerPlayType(matchId, matchInfoList, gameMatchType, liveVideo)

    var clickOdd: Odd? = null

    fun onClickBet(
        view:View,
        matchInfo: MatchInfo?,
        odd: Odd,
        playCateCode: String,
        playCateName: String = "",
        betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?
    ) {
        clickOdd = odd
        clickListenerBet(view,matchInfo, odd, playCateCode, playCateName, betPlayCateNameMap)
    }


    fun onClickFavorite(matchId: String?) = clickListenerFavorite(matchId)

    fun onClickStatistics(matchId: String?) = clickListenerStatistics(matchId)

    fun onClickCsTabListener(playCate: PlayCate, matchOdd: MatchOdd) = clickCsTabListener(playCate, matchOdd)


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