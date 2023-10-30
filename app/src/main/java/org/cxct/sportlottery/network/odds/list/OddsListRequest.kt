package org.cxct.sportlottery.network.odds.list

import org.cxct.sportlottery.common.proguards.KeepMembers

@KeepMembers
data class OddsListRequest(
    val gameType: String,
    val matchType: String,
    val playCateMenuCode: String,
    val leagueIdList: List<String> = listOf(),
    val matchIdList: List<String> = listOf(),
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val categoryCodeList: List<String>?=null,//电竞玩法里面的具体游戏code
)
