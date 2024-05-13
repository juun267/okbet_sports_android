package org.cxct.sportlottery.network.odds


import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import kotlinx.android.parcel.Parcelize
import org.cxct.sportlottery.network.common.MatchInfo
import org.cxct.sportlottery.network.service.match_status_change.MatchStatus

@Parcelize
@JsonClass(generateAdapter = true)
@KeepMembers
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
    @Json(name = "odds")
    internal var originalOdds: String? = null, //赔率(load data)
    @Json(name = "playCateNum")
    override var playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "status")
    var status: Int?,//赛事状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
    @Json(name = "leagueId")
    val leagueId: String? = null,
    @Json(name = "leagueName")
    var leagueName: String? = null,
    @Json(name = "shortName")
    var shortName: String? = null,
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
    @Json(name = "source")
    val source: Int?,
    @Json(name = "parlay")
    val parlay: Int?, //parlay (是否可以参加过关，0：否，1：是)
    @Json(name = "homeIcon")
    val homeIcon: String? = null,
    @Json(name = "awayIcon")
    val awayIcon: String? = null,
    @Json(name = "isLive")
    val isLive: Int? = 0,//是否有主播直播 0：否，1：是
    @Json(name = "roundNo")
    val roundNo: String? = null,//主播房间号
    @Json(name = "streamerIcon")
    val streamerIcon: String? = null,//主播头像
    @Json(name = "streamerName")
    val streamerName: String? = null,//主播名字
    @Json(name = "frontCoverUrl")
    val frontCoverUrl: String? = null,//封面路径
    @Json(name = "extInfo")
    val extInfo: String? = null,
) : Parcelable, MatchInfo {
    //Live
    var isInPlay: Boolean? = false
    override var awayScore: String? = null //客队分数
    override var homeScore: String? = null //主队分数
    override var statusName18n: String? = null //状态名称
    override var leagueTime: Int? = null     // 秒为单位
    override var socketMatchStatus: Int? = null //赛事阶段状态id
    override var stopped: Int? = null//賽事是否暫停倒數计时 1:是 ，0：否

    //At Start
    var isAtStart: Boolean? = false
    var remainTime: Long? = null
    @Transient
    var leagueTimeRecode: Long = -1      // 时间标记，如果相同就不进行重复计时

    //Other
    var startDateDisplay: String? = null
    var startTimeDisplay: String? = null

    var isFavorite = false
    var expand = true

    override var homeTotalScore: Int? = null
    override var awayTotalScore: Int? = null

    //TN
    override var homePoints: String? = null
    override var awayPoints: String? = null

    //FT
    override var homeCards: Int = 0
    override var awayCards: Int = 0
    override var homeYellowCards: Int = 0
    override var awayYellowCards: Int = 0
    override var homeCornerKicks: Int = 0
    override var awayCornerKicks: Int = 0
    override var homeHalfScore: String? = null
    override var awayHalfScore: String? = null

    //BB
    override var attack: String? = null
    override var halfStatus: Int? = null //棒球半場狀態: 0: 上半場, 1:下半場, 2:不顯示
    override var firstBaseBag: Int? = null
    override var secBaseBag: Int? = null
    override var thirdBaseBag: Int? = null
    override var outNumber: Int? = null

    override var awayOver: String? = null //客队分数
    override var homeOver: String? = null //主队分数

    override var matchStatusList: List<MatchStatus>? = null

    //联赛图标
    var categoryIcon: String? = null
    var categoryCode: String? = null

    var pullRtmpUrl: String? = null
    var betCount: Int=0 //endcard模块会有下注数量
}