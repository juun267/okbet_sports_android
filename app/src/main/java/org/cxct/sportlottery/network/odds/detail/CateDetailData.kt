package org.cxct.sportlottery.network.odds.detail


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.odds.Odd

@JsonClass(generateAdapter = true)
data class CateDetailData(
    @Json(name = "name")
    val name: String,
    @Json(name = "odds")
    val odds: MutableList<Odd?>,
    @Json(name = "typeCodes")
    val typeCodes: String,
    @Json(name = "nameMap")
    val nameMap: Map<String?, String?>? = null, //保存各语系name对应值的map
    @Json(name = "rowSort")
    val rowSort: Int
){

}