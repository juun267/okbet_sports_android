package org.cxct.sportlottery.network.odds.list


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.outright.odds.DynamicMarket
import org.cxct.sportlottery.ui.common.PlayCateMapItem
import org.cxct.sportlottery.util.sortOddsMap

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchOdd(
    @Json(name = "betPlayCateNameMap")
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "playCateNameMap")
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    ),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: Map<String, DynamicMarket>? = null,
    @Json(name = "quickPlayCateList")
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null,
    @Json(name = "oddsSort")
    override var oddsSort: String? = null
) : MatchOdd {

    override val oddsEps: EpsOdd? = null
    var rvScrollPos: Int? = null

    @Deprecated("之後翻譯都要改用playCateNameMap，下注顯示用betPlayCateNameMap")
    override var playCateMappingList: List<PlayCateMapItem>? = null

    var isExpand = false
    var leagueTime: Int? = null
    var leagueName: String = ""
    var stopped: Int? = 0

    var positionButtonPage = 0

    var quickPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null //足球快捷玩法的翻譯

    fun sortOddsMap() {
        this.oddsMap?.sortOddsMap()
    }

    var runningTime: String = ""

    var csTabSelected: PlayCate = PlayCate.CS
}

enum class TimeCounting(val value: Int) {
    STOP(1), CONTINUE(0)
}