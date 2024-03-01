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
    var leagueShowByTeamName: Boolean = false //依隊名的聯賽過濾顯示(賽事僅顯示匹配到的隊伍), 顯示:true , 不顯示:false
    var titleExpanded: Boolean = true //聯賽是否展開, true:展開, false:收合
    var matchExpanded: Boolean = false
    var isLastMatchData: Boolean = false //是否最後一筆matchData
}

enum class ListType {
    TITLE,
    MATCH,
    MATCH_FT,
    DETAIL,
    FIRST_ITEM_FT, //足球
    FIRST_ITEM_BK, //籃球
    FIRST_ITEM_TN, //網球
    FIRST_ITEM_BM, //羽球
    FIRST_ITEM_VB, //排球
    FIRST_ITEM_TT, //桌球
    FIRST_ITEM_IH, //冰球
    FIRST_ITEM_BX, //拳擊
    FIRST_ITEM_CB, //台球
    FIRST_ITEM_CK, //板球
    FIRST_ITEM_BB, //棒球
    FIRST_ITEM_RB, //橄欖球
    FIRST_ITEM_MR, //賽車
    FIRST_ITEM_GF, //高爾夫球
    FIRST_ITEM_ES, //電競
    FIRST_ITEM_AFT, //美式足球
}

