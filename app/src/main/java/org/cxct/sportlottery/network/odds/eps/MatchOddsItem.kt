package org.cxct.sportlottery.network.odds.eps

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@JsonClass(generateAdapter = true)
data class MatchOddsItem(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    override val oddsEps: Odds?
) : MatchOdd {
    override val oddsMap: MutableMap<String, MutableList<Odd?>?> = mutableMapOf()

    override val quickPlayCateList: MutableList<QuickPlayCate>? = null

    override var playCateMappingList: List<PlayCateMapItem>? = null

    override val oddsSort: String? = null
}
