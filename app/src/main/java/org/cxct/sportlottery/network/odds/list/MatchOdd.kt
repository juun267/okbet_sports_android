package org.cxct.sportlottery.network.odds.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.outright.odds.DynamicMarket

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: MatchInfo? = null,
    @Json(name = "odds")
    var odds: @RawValue MutableMap<String, MutableList<Odd?>> = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    ),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: @RawValue Map<String, DynamicMarket>? = null,
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: @RawValue List<QuickPlayCate>? = null
) : Parcelable {
    var isExpand = false
    var leagueTime: Int? = null
    var matchStatusList: List<MatchStatus> = listOf()

    var positionButtonPage = 0
}