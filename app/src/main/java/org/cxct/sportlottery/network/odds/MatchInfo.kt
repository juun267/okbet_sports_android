package org.cxct.sportlottery.network.odds


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.network.common.MatchInfo

@Parcelize
@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "gameType")
    var gameType: String?,
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "id")
    override val id: String, //赛事或赛季id
    @Json(name = "playCateNum")
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    var status: Int?,
    @Json(name = "leagueId")
    val leagueId: String? = null,
    @Json(name = "leagueName")
    val leagueName: String? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "img")
    var img: String? = null,
    @Json(name = "msg")
    var msg: String? = null,
    @Json(name = "liveVideo")
    val liveVideo: Int? = null,
    @Json(name = "neutral")
    val neutral: Int? = null,
    @Json(name = "eps")
    val eps: Int? = null,

    ) : Parcelable, MatchInfo {
    //Live
    var isInPlay: Boolean? = false
    override var awayScore: Int? = null //客队分数
    override var homeScore: Int? = null //主队分数
    override var statusName: String? = null //状态名称
    override var leagueTime: Int? = null

    //At Start
    var isAtStart: Boolean? = false
    var remainTime: Long? = null

    //Other
    var startDateDisplay: String? = null
    var startTimeDisplay: String? = null

    var isFavorite = false

    override var homeTotalScore: Int? = null
    override var awayTotalScore: Int? = null

    //VB & TN
    override var homePoints: Int? = null
    override var awayPoints: Int? = null

    //FT
    override var homeCornerKicks: Int? = null
    override var awayCornerKicks: Int? = null
    override var homeCards: Int? = null
    override var awayCards: Int? = null
    override var homeYellowCards: Int? = null
    override var awayYellowCards: Int? = null


}