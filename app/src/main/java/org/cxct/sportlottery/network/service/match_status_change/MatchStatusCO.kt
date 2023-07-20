package org.cxct.sportlottery.network.service.match_status_change


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchStatusCO(
    @Json(name = "gameType")
    val gameType: String?,
    @Json(name = "awayCards")
    val awayCards: Int? = null,
    @Json(name = "awayCornerKicks")
    val awayCornerKicks: Int = 0,
    @Json(name = "awayScore")
    val awayScore: Int? = null,
    @Json(name = "awayTotalScore")
    val awayTotalScore: Int? = null,
    @Json(name = "awayYellowCards")
    val awayYellowCards: Int? = null,
    @Json(name = "dataId")
    val dataId: String? = "",
    @Json(name = "homeCards")
    val homeCards: Int? = null,
    @Json(name = "homeCornerKicks")
    val homeCornerKicks: Int = 0,
    @Json(name = "homeScore")
    val homeScore: Int? = null,
    @Json(name = "homeTotalScore")
    val homeTotalScore: Int? = null,
    @Json(name = "homePoints")
    val homePoints: String? = null,
    @Json(name = "awayPoints")
    val awayPoints: String? = null,
    @Json(name = "homeHalfScore")
    val homeHalfScore: String? = null,
    @Json(name = "awayHalfScore")
    val awayHalfScore: String? = null,
    @Json(name = "homeYellowCards")
    val homeYellowCards: Int? = null,
    @Json(name = "latestStatus")
    val latestStatus: Int? = null,
    @Json(name = "matchId")
    val matchId: String? = "",
    @Json(name = "status")
    val status: Int? = null,
    @Json(name = "statusName")
    val statusName: String? = "",
    @Json(name = "statusNameI18n")
    val statusNameI18n: Map<String?, String?>? = mapOf(),
    @Json(name = "time")
    val time: String? = "",
    @Json(name = "homeOver")
    val homeOver: String? = null,
    @Json(name = "awayOver")
    val awayOver: String? = null,
    @Json(name = "homeOut")
    val homeOut: String? = null,
    @Json(name = "awayOut")
    val awayOut: String? = null,
    @Json(name = "attack")
    val attack: String? = null, //攻击方 H:主队 C:客队
    @Json(name = "halfStatus")
    val halfStatus: Int? = null, //滚球半场状态 (0:上半场 1:下半场)
    @Json(name = "firstBaseBag")
    val firstBaseBag: Int? = null, //一垒是否有人(0沒人/1有人)
    @Json(name = "secBaseBag")
    val secBaseBag: Int? = null,
    @Json(name = "thirdBaseBag")
    val thirdBaseBag: Int? = null,
    @Json(name = "outNumber")
    val outNumber: Int? = null, //目前几人出局
    @Json(name = "periods")
    val periods: List<MatchStatus>? = null, //局比分
)