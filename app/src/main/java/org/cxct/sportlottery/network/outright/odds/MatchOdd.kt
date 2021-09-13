package org.cxct.sportlottery.network.outright.odds

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.Odds
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override var oddsMap: MutableMap<String, MutableList<Odd?>> = mutableMapOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: @RawValue Map<String, DynamicMarket>,
    @Json(name = "oddsList")
    val oddsList: @RawValue List<String?>? = listOf(), //TODO Cheryl : 目前回傳都是null, 待測試
    @Json(name = "quickPlayCateList")
    override val quickPlayCateList: @RawValue List<QuickPlayCate>? = listOf(), //(新)赛事可玩的快捷玩法列表
    @Json(name = "oddsSort")
    override val oddsSort: String? = null
) : MatchOdd, Parcelable {
    @IgnoredOnParcel
    override val oddsEps: Odds? = null

    @IgnoredOnParcel
    var startDate: String = ""

    @IgnoredOnParcel
    var startTime: String = ""
}