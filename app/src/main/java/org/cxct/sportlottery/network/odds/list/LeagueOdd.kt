package org.cxct.sportlottery.network.odds.list

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.IgnoredOnParcel
import org.cxct.sportlottery.common.extentions.isEmptyStr
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.GameType
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true)
@KeepMembers
data class LeagueOdd(
    @Json(name = "league")
    val league: League,
    @Json(name = "sort")
    val sort: Int?,
    @Json(name = "matchOdds")
    val matchOdds: MutableList<MatchOdd> = mutableListOf(),
    @Json(name = "playCateNameMap")
    var playCateNameMap: MutableMap<String?, Map<String?, String?>?>? = null,
): BaseExpandNode() {
    init {
        matchOdds.forEach { it.parentNode = this }
    }
    var gameType: GameType? = null

    @IgnoredOnParcel
    override val childNode by lazy {
        val list = mutableListOf<BaseNode>()
        matchOdds.forEach {
            list.add(it)
            it.matchInfo?.let { matchInfo ->
                if (matchInfo.leagueName.isEmptyStr()) {
                    matchInfo.leagueName = league.name
                }
                matchInfo.categoryCode = league.categoryCode
                matchInfo.shortName = league.shortName
            }
        }
        return@lazy list
    }

}