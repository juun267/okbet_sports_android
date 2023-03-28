package org.cxct.sportlottery.network.sport.publicityRecommend


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.MatchOdd
import org.cxct.sportlottery.network.common.MatchType
import org.cxct.sportlottery.network.common.PlayCate
import org.cxct.sportlottery.network.odds.MatchInfo
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.odds.eps.EpsOdd
import org.cxct.sportlottery.network.odds.list.QuickPlayCate

@JsonClass(generateAdapter = true) @KeepMembers
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
    val menuList: List<Menu> = arrayListOf(),
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
}