package org.cxct.sportlottery.network.third_game.third_games.hot


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguard.KeepMembers
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@JsonClass(generateAdapter = true)
@KeepMembers
data class HotMatchInfo(
    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "gameType")
    val gameType: String,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "homeIcon")
    val homeIcon: String?,
    @Json(name = "awayIcon")
    val awayIcon: String?,
    @Json(name = "id")
    val id: String,
    @Json(name = "leagueId")
    var leagueId: String,
    @Json(name = "leagueName")
    var leagueName: String? = null,
    @Json(name = "liveVideo")
    val liveVideo: Int,
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
    val tvId: String,
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
) : MatchOdd {
    var matchType: MatchType? = null
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
    override var matchInfo: MatchInfo? = null
    override var oddsMap: MutableMap<String, MutableList<Odd>?>? = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    )
    override var oddsSort: String? = null
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null
    override val oddsEps: EpsOdd? = null

    var unfold: Int = FoldState.UNFOLD.code

    var runningTime: String = ""

    fun getBuildMatchInfo(): MatchInfo {
        if (matchInfo == null) {
            matchInfo = MatchInfo(
                gameType = gameType,
                awayName = awayName,
                endTime = null,
                homeName = homeName,
                id = id,
                playCateNum = playCateNum,
                startTime = startTime,
                status = status,
                leagueId = leagueId,
                leagueName = leagueName,
                name = null,
                img = null,
                msg = null,
                liveVideo = liveVideo,
                neutral = neutral,
                eps = null,
                spt = spt,
                trackerId = trackerId,
                source = null,
                parlay = null,
                homeIcon = homeIcon,
                awayIcon = awayIcon,
                isLive = isLive,
                roundNo = roundNo,
                streamerIcon = streamerIcon,
                streamerName = streamerName,
                frontCoverUrl = frontCoverUrl
            )
        }
        return matchInfo!!
    }
}