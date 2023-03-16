package org.cxct.sportlottery.network.vip.thirdRebates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.proguard.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class Debate(
    @Json(name = "debate")
    val debate: Double?,
    @Json(name = "id")
    val id: Double,
    @Json(name = "maxDebateMoney")
    val maxDebateMoney: Double?,
    @Json(name = "maxMoney")
    val maxMoney: Double?,
    @Json(name = "minMoney")
    val minMoney: Double?,
    @Json(name = "userLevelId")
    val userLevelId: Int,
    @Json(name = "userLevelName")
    val userLevelName: String
) {
    var isTitle: Boolean = false
    var isLastContent: Boolean = false
    var isNullTail: Boolean = false
    var levelIndex: Int = 0
}