package org.cxct.sportlottery.ui.results

import org.cxct.sportlottery.network.matchresult.list.League
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList

/**
 * 重組資料結構將賽果結算資料扁平化
 */
data class MatchResultData(
    val dataType: ListType,
    val titleData: League? = null,
    val matchData: Match? = null,
    val matchDetailData: MatchResultPlayList? = null
) {
    var leagueShow: Boolean = false //聯賽過濾顯示, 顯示:true , 不顯示:false
    var titleExpanded: Boolean = true //聯賽是否展開, true:展開, false:收合
    var matchExpanded: Boolean = false
}

enum class ListType { TITLE, MATCH, DETAIL, FIRST_ITEM_FT, FIRST_ITEM_BK, FIRST_ITEM_TN, FIRST_ITEM_BM, FIRST_ITEM_VB, NO_DATA }