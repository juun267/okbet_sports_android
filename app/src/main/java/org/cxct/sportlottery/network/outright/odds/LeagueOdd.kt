package org.cxct.sportlottery.network.outright.odds


import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.odds.League

@JsonClass(generateAdapter = true) @KeepMembers
data class LeagueOdd(
    @Json(name = "league")
    val league: League?,
    @Json(name = "matchOdds")
    val matchOdds: List<MatchOdd>? = listOf(),
): BaseExpandNode() {
    var isExpand = false
    override val childNode: MutableList<BaseNode>? = matchOdds as MutableList<BaseNode>?
}