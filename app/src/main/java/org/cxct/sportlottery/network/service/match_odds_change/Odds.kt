package org.cxct.sportlottery.network.service.match_odds_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.Odd

@JsonClass(generateAdapter = true) @KeepMembers
data class Odds(
    @Json(name = "typeCodes")
    val typeCodes: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: MutableList<Odd?>?,
    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map
    @Json(name = "rowSort")
    val rowSort: Int
)