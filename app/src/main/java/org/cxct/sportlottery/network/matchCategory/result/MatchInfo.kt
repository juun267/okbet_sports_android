package org.cxct.sportlottery.network.matchCategory.result


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.MatchInfo
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus

@JsonClass(generateAdapter = true) @KeepMembers
data class MatchInfo(
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "eps")
    val eps: Int?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "id")
    override val id: String,
    @Json(name = "img")
    val img: String?,
    @Json(name = "leagueId")
    val leagueId: String?,
    @Json(name = "leagueName")
    val leagueName: String?,
    @Json(name = "msg")
    val msg: String?,
    @Json(name = "liveVideo")
    val liveVideo: Int?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "playCateNum")
    override var playCateNum: Int?,
    @Json(name = "spt")
    val spt: Int?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "trackerId")
    val trackerId: String?,
    @Json(name = "source")
    val source: Int?,
    @Json(name = "parlay")
    val parlay: Int?, //parlay (是否可以参加过关，0：否，1：是)
    override var homeOver: String?, override var awayOver: String?,
) : MatchInfo {

    override val gameType: String? = null

    override var awayScore: String? = null

    override var homeScore: String? = null

    override var statusName18n: String? = null

    override var leagueTime: Int? = null

    override var socketMatchStatus: Int? = null

    override var homeTotalScore: Int? = null

    override var awayTotalScore: Int? = null

    override var homePoints: String? = null

    override var awayPoints: String? = null

    override var homeCards: Int? = null

    override var awayCards: Int? = null

    override var homeYellowCards: Int? = null

    override var awayYellowCards: Int? = null

    override var homeCornerKicks: Int? = null

    override var awayCornerKicks: Int? = null

    override var homeHalfScore: String? = null
    override var awayHalfScore: String? = null


    override var stopped: Int? = null//賽事是否暫停倒數计时 1:是 ，0：否

    /**
     * 局比分
     */
    override var matchStatusList: List<MatchStatus>? = null

    override var attack: String? = null

    override var halfStatus: Int? = null

    override var firstBaseBag: Int? = null

    override var secBaseBag: Int? = null

    override var thirdBaseBag: Int? = null

    override var outNumber:Int? = null
}