package org.cxct.sportlottery.network.bet.add.betReceipt


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.common.enums.OddsType
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.common.proguards.KeepMembers

//不可與org.cxct.sportlottery.network.bet.add.Row共用，因matchOdds底下hkOdds有可能為null
@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
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
    var status: Int?, //投注失败后台返回status == 7
    @Json(name = "winnable")
    val winnable: Double?,
    @Json(name = "reason")
    val reason: String? = "",
    @Json(name = "code")
    val code :String? = ""
    ):Parcelable {
    var matchType: MatchType? = null
    var oddsType: OddsType? = null
    var cashoutStatus: Int? =null
    fun isFailed() = 7 == status
}