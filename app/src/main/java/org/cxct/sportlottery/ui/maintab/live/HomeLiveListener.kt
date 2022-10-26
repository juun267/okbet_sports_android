package org.cxct.sportlottery.ui.maintab.live

import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.MatchLiveData

open class HomeLiveListener(
    private val onItemClickListener: (item: MatchLiveData) -> Unit,
    private val onClickBetListener: (gameType: String, matchType: MatchType, matchInfo: MatchInfo?, odd: Odd, playCateCode: String, playCateName: String, betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>?, playCateMenuCode: String?) -> Unit,
    private val onClickLiveListener: (matchId: String, roundNo: String) -> Unit,
) {
    fun onItemClickListener(item: MatchLiveData) = onItemClickListener.invoke(item)
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

    fun onClickLiveListener(matchId: String, roundNo: String) =
        onClickLiveListener.invoke(matchId, roundNo)
}

