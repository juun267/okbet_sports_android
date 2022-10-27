package org.cxct.sportlottery.network.third_game.third_games.hot


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate
import org.cxct.sportlottery.ui.common.PlayCateMapItem

@JsonClass(generateAdapter = true)
data class MatchInfo(
    @Json(name = "id")//赛事或赛季id
    val id: String,
     @Json(name = "leagueId")//联赛ID
    val leagueId: String,
    @Json(name = "leagueName")//联赛名称
    val leagueName: String,


    @Json(name = "awayName")
    val awayName: String,
    @Json(name = "endTime")
    val endTime: String?,
    @Json(name = "homeName")
    val homeName: String,
    @Json(name = "playCateNum")//该赛事之全部可投注玩法类数量
    val playCateNum: Int?,
    @Json(name = "startTime")
    val startTime: String,
    @Json(name = "status")
    val status: Int, //赛事状态 0：未开始，1：比赛中，2：已结束，3：延期，4：已取消
    @Json(name = "img")
    val img: String,
    @Json(name = "msg")
    val msg: String,
    @Json(name = "homeIcon")
    val homeIcon: String,
    @Json(name = "homeIconSvg")
    val homeIconSvg: String,
    @Json(name = "awayIcon")
    val awayIcon: String,
    @Json(name = "awayIconSvg")
    val awayIconSvg: String,

    @Json(name = "roundNo")//房间号
    val roundNo: String,
    @Json(name = "streamerIcon")//主播头像
    val streamerIcon: String,
    @Json(name = "StreamerName")//主播名称
    val StreamerName: String,
     @Json(name = "frontCoverUrl")//封面路径
    val frontCoverUrl: String,
     @Json(name = "gameType")//游戏类型
    val gameType: Long,
    @Json(name = "source")//数据源
    val source: Long


): MatchOdd {
    var matchType: MatchType? = null
    override var betPlayCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
    override var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null
    override var matchInfo: MatchInfo? = null
    override var oddsMap: MutableMap<String, MutableList<Odd?>?>? = mutableMapOf(
        PlayCate.HDP.value to mutableListOf(),
        PlayCate.OU.value to mutableListOf(),
        PlayCate.SINGLE.value to mutableListOf()
    )
    override var oddsSort: String? = null
    override var quickPlayCateList: MutableList<QuickPlayCate>? = null
    override val oddsEps: EpsOdd? = null
    override var playCateMappingList: List<PlayCateMapItem>? = null

    var unfold: Int = FoldState.UNFOLD.code

    var runningTime: String = ""
}