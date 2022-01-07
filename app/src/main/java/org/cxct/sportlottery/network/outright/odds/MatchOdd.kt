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
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchOdd(
    @Json(name = "matchInfo")
    override val matchInfo: MatchInfo?,
    @Json(name = "odds")
    override val oddsMap: MutableMap<String, MutableList<Odd?>?> = mutableMapOf(),
    @Json(name = "dynamicMarkets")
    val dynamicMarkets: @RawValue Map<String, DynamicMarket>,
    @Json(name = "oddsList")
    val oddsList: @RawValue List<String?>? = listOf(), //TODO Cheryl : 目前回傳都是null, 待測試
    @Json(name = "quickPlayCateList")
    override val quickPlayCateList: @RawValue MutableList<QuickPlayCate>? = mutableListOf(), //(新)赛事可玩的快捷玩法列表
    @Json(name = "oddsSort")
    override val oddsSort: String? = null,
    @Json(name = "betPlayCateNameMap")
    override val betPlayCateNameMap: Map<String?, Map<String?, String?>?>? = mapOf(),
    @Json(name = "playCateNameMap")
    override val playCateNameMap: Map<String?, Map<String?, String?>?>? = mapOf(),
    ) : MatchOdd,

    Parcelable {
    @IgnoredOnParcel
    override val oddsEps: Odds? = null

    @IgnoredOnParcel
    override var playCateMappingList: List<PlayCateMapItem>? = null

    @IgnoredOnParcel
    var startDate: String = ""

    @IgnoredOnParcel
    var startTime: String = ""

    @IgnoredOnParcel
    override var stopped: Int? = null//賽事是否暫停倒數计时 1:是 ，0：否
}