package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@JsonClass(generateAdapter = true)
data class MatchOddsItem(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    val oddsEps: Odds?
) : MatchOdd {
    override val odds: MutableMap<String, MutableList<Odd?>> = mutableMapOf()

    override val quickPlayCateList: List<QuickPlayCate>? = null

    //    override var oddsMap =
//        mutableMapOf(Pair(PlayCate.EPS.value, oddsEps?.eps?.toMutableList() ?: mutableListOf()))
}
