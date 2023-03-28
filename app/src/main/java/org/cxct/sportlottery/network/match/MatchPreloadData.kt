package org.cxct.sportlottery.network.match


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.MatchType

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchPreloadData(
    @Json(name = "datas")
    val datas: List<Data>,
    @Json(name = "num")
    val num: Int
) {
    var matchType: MatchType? = null
}