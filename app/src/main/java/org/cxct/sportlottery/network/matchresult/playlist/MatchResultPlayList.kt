package org.cxct.sportlottery.network.matchresult.playlist


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.util.replaceSpecialChar

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchResultPlayList(
    @Json(name = "matchId")
    val matchId: String,
    @Json(name = "playCateCode")
    val playCateCode: String,
    @Json(name = "playCateId")
    val playCateId: Int,
    @Json(name = "playCateName")
    var playCateName: String,
    @Json(name = "playCode")
    val playCode: String,
    @Json(name = "playId")
    val playId: Int,
    @Json(name = "playName")
    var playName: String,
    @Json(name = "resultStatus")
    val resultStatus: Int,
    @Json(name = "spread")
    val spread: String?,
    @Json(name = "updateTime")
    val updateTime: Long?
){
    init {
        playCateName = playCateName.replaceSpecialChar("\n")
        playName = playName.replaceSpecialChar("\n")
    }
}