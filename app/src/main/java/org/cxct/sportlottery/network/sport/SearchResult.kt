package org.cxct.sportlottery.network.sport

data class SearchResult(
    var sportTitle: String = "",
    var gameType:String = "",
    var searchResultLeague:List<SearchResultLeague> = mutableListOf()

) {
    data class SearchResultLeague(
        var league:String = "",
        var leagueMatchList: List<SearchResponse.Row.LeagueMatch.MatchInfo> = mutableListOf(),
    )
}