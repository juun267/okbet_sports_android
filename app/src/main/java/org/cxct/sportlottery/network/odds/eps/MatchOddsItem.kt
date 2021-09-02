package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.list.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@JsonClass(generateAdapter = true)
data class MatchOddsItem(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    val oddsEps: Odds?
) : MatchOdd {
    override var oddsMap =
        mutableMapOf(Pair(PlayCate.EPS.value, oddsEps?.eps?.toMutableList() ?: mutableListOf()))
    override val quickPlayCateList: List<QuickPlayCate>? = null
}
