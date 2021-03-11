package org.cxct.sportlottery.ui.results

import org.cxct.sportlottery.network.matchresult.list.League
import org.cxct.sportlottery.network.matchresult.list.Match
import org.cxct.sportlottery.network.matchresult.playlist.MatchResultPlayList
import org.cxct.sportlottery.network.outright.Result
import org.cxct.sportlottery.network.outright.Season

/**
 * 重組資料結構將賽果結算資料扁平化
 */
data class OutrightResultData(
    val dataType: OutrightType,
    val seasonData: Season? = null,
    val outrightData: Result? = null,
) {
    var seasonShow: Boolean = false //聯賽過濾顯示, 顯示:true , 不顯示:false
    var seasonExpanded: Boolean = false
}

enum class OutrightType { TITLE, OUTRIGHT }