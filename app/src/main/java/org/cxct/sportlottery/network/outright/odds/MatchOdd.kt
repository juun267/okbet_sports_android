package org.cxct.sportlottery.network.outright.odds

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    val
    matchInfo: MatchInfo?,
    @Json(name = "odds")
    val odds: @RawValue Map<String, List<Odd?>> = mapOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: @RawValue Map<String, DynamicMarket>,
    @Json(name = "oddsList")
    val oddsList: @RawValue List<String?>? = listOf(), //TODO Cheryl : 目前回傳都是null, 待測試
    @Json(name = "quickPlayCateList")
    val quickPlayCateList: @RawValue List<QuickPlayCate?>? = listOf(), //(新)赛事可玩的快捷玩法列表
) : Parcelable {
    var startDate: String = ""
    var startTime: String = ""
}