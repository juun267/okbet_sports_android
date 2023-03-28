package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.League
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchLiveData(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo,
    @Json(name = "oddsSort")
    override val oddsSort: String?,
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>?,
    @Json(name = "sportName")
    val sportName: String,
) : MatchOdd {
    var matchType: MatchType? = null
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = mutableMapOf(
        PlayCate.SINGLE.value to mutableListOf()
    )
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null
    override val oddsEps: EpsOdd? = null
    var runningTime: String = ""

}