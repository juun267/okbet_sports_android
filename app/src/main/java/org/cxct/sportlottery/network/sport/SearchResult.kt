package org.cxct.sportlottery.network.sport

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode
import org.cxct.sportlottery.common.proguards.KeepMembers


@KeepMembers
data class SearchResult(
    var sportTitle: String = "",
    var gameType:String = "",
    var searchResultLeague:MutableList<SearchResultLeague> = mutableListOf(),
): BaseNode() {

    override val childNode: MutableList<BaseNode>?
        get() = searchResultLeague as MutableList<BaseNode>?

    data class SearchResultLeague(
        var league:String = "",
        var icon:String = "",
        var leagueMatchList: MutableList<SearchResponse.Row.LeagueMatch.MatchInfo> = mutableListOf(),
    ): BaseExpandNode() {

        override val childNode: MutableList<BaseNode>?
            get() = leagueMatchList as MutableList<BaseNode>?
    }
}