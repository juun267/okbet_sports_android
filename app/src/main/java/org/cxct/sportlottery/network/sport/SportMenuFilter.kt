package org.cxct.sportlottery.network.sport

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class SportMenuFilter(
    @Json(name = "code")
    val code: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "selectionType")
    val selectionType: Int,
    @Json(name = "num")
    val num: Int,
   // val playCateList: List<PlayCate>,
  //  val nameMap: NameMap,
    @Json(name = "oddsSort")
    val oddsSort: String,
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = mutableMapOf(),
    @Json(name = "playCateMap")
    var playCateMap: MutableMap<String?, PlayCate>? = mutableMapOf(),
    ) {
    data class PlayCate(
        @Json(name = "code")
        val code: String,
        @Json(name = "name")
        val name: String,
        @Json(name = "playCateNameMap")
        var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = mutableMapOf()
    )
    data class NameMap(
        val mapKey: String
    )
}