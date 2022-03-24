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
    override var gameType: String?,
    @Json(name = "awayName")
    val awayName: String?,
    @Json(name = "endTime")
    val endTime: Long?,
    @Json(name = "homeName")
    val homeName: String?,
    @Json(name = "id")
    override val id: String, //赛事或赛季id
    @Json(name = "playCateNum")
    override var playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long?,
    @Json(name = "status")
    var status: Int?,//赛事状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
    @Json(name = "leagueId")
    val leagueId: String? = null,
    @Json(name = "leagueName")
    var leagueName: String? = null,
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
    @Json(name = "spt")
    val spt: Int? = null,
    @Json(name = "trackerId")
    val trackerId: String? = null,//动画映射id

    ) : Parcelable, MatchInfo {
    //Live
    var isInPlay: Boolean? = false
    override var awayScore: String? = null //客队分数
    override var homeScore: String? = null //主队分数
    override var statusName18n: String? = null //状态名称
    override var leagueTime: Int? = null
    override var socketMatchStatus: Int? = null //赛事阶段状态id
    override var stopped: Int? = null//賽事是否暫停倒數计时 1:是 ，0：否

    //At Start
    var isAtStart: Boolean? = false
    var remainTime: Long? = null
    var hasRefreshed: Boolean? = false

    //Other
    var startDateDisplay: String? = null
    var startTimeDisplay: String? = null
    var timeDisplay: String? = null

    var isFavorite = false

    override var homeTotalScore: Int? = null
    override var awayTotalScore: Int? = null

    //TN
    override var homePoints: Int? = null
    override var awayPoints: Int? = null

    //FT
    override var homeCards: Int? = null
    override var awayCards: Int? = null

    //999
    var scoreStatus:Int? = 0


}