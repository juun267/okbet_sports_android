package org.cxct.sportlottery.network.odds.list


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.network.common.PlayType
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val matchInfo: @RawValue MatchInfo? = null,
    @Json(name = "odds")
    var odds: @RawValue MutableMap<String, MutableList<Odd?>> = mutableMapOf(
        PlayType.HDP.code to mutableListOf(),
        PlayType.OU.code to mutableListOf(),
        PlayType.X12.code to mutableListOf()
    )
) : Parcelable {
    var isExpand = false
    var leagueTime: Int? = null
    var matchStatusList: List<MatchStatus> = listOf()
}