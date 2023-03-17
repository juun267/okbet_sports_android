package org.cxct.sportlottery.network.odds.list


import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import org.cxct.sportlottery.proguard.KeepMembers
import org.cxct.sportlottery.network.common.FoldState
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.League
import org.cxct.sportlottery.network.odds.Odd
import org.cxct.sportlottery.network.outright.odds.CategoryOdds

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueOdd(
    @Json(name = "league")
    val league: League,
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOdd> = mutableListOf(),
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "unfold")
    var unfold: Int? = FoldState.UNFOLD.code, // 服务端字段，不读取该状态
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
): BaseExpandNode() {
    var unfoldStatus: Int = FoldState.UNFOLD.code
    var searchMatchOdds = listOf<MatchOdd>()
    var gameType: GameType? = null

    override val childNode: MutableList<BaseNode>? = matchOdds as MutableList<BaseNode>?

    @Transient
    @IgnoredOnParcel
    var oddIdsMap: MutableMap<String, MutableMap<String, Odd>> = mutableMapOf()  //用于本地计算

    @Transient
    @IgnoredOnParcel
    var categoryOddsMap = mutableMapOf<String, CategoryOdds>()    //用于本地计算

}