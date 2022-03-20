package org.cxct.sportlottery.network.sport.publicityRecommend


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.MatchType

@JsonClass(generateAdapter = true)
data class Recommend(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "categoryIcon")
    val categoryIcon: String,
    @Json(name = "categoryName")
    val categoryName: String,
    @Json(name = "eps")
    val eps: Int,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "leagueId")
    val leagueId: String,
    @Json(name = "leagueName")
    val leagueName: String,
    @Json(name = "liveVideo")
    val liveVideo: Int,
    @Json(name = "matchNum")
    val matchNum: Int,
    @Json(name = "menuList")
    val menuList: List<Menu>,
    @Json(name = "neutral")
    val neutral: Int,
    @Json(name = "playCateNum")
    val playCateNum: Int,
    @Json(name = "spt")
    val spt: Int,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    val status: Int,
    @Json(name = "streamId")
    val streamId: String?,
    @Json(name = "trackerId")
    val trackerId: String?,
    @Json(name = "tvId")
    val tvId: String
) {
    var matchType: MatchType? = null
}