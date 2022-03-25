package org.cxct.sportlottery.network.bet.add.betReceipt


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.ui.menu.OddsType

//不可與org.cxct.sportlottery.network.bet.add.Row共用，因matchOdds底下hkOdds有可能為null
@JsonClass(generateAdapter = true)
data class BetResult(
    @Json(name = "eventType")
    val eventType: Int?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>?,
    @Json(name = "num")
    val num: Int?,
    @Json(name = "orderNo")
    val orderNo: String?,
    @Json(name = "parlay")
    val parlay: Int?,
    @Json(name = "parlayType")
    val parlayType: String?,
    @Json(name = "stake")
    val stake: Double?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "winnable")
    val winnable: Double?,
    @Json(name = "reason")
    val reason: String? = ""
    ) {
    var matchType: MatchType? = null
    var oddsType: OddsType? = null
}