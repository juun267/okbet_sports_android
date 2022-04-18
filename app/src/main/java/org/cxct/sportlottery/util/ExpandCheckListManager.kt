package org.cxct.sportlottery.util

/**
 * @author kevin
 * @create 2022/4/18
 * @description
 * String 聯賽ID
 * Boolean 是否展開
 */
object ExpandCheckListManager {

    var expandCheckList: MutableMap<String, Boolean> = mutableMapOf()

    fun setLeagueExpandStatus(leagueId: String, isExpand: Boolean){
        expandCheckList[leagueId] = isExpand
    }

}