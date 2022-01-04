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

enum class ListType { TITLE, MATCH, DETAIL, FIRST_ITEM_FT, FIRST_ITEM_BK, FIRST_ITEM_TN, FIRST_ITEM_BM, FIRST_ITEM_VB, FIRST_ITEM_TT ,FIRST_ITEM_IH ,FIRST_ITEM_BX ,FIRST_ITEM_CB ,FIRST_ITEM_CK ,FIRST_ITEM_BB ,FIRST_ITEM_RB ,FIRST_ITEM_MR ,FIRST_ITEM_GF ,FIRST_ITEM_AFT ,NO_DATA }

