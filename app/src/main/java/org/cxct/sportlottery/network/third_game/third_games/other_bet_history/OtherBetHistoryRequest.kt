package org.cxct.sportlottery.network.third_game.third_games.other_bet_history

import org.cxct.sportlottery.common.proguards.KeepMembers
import org.cxct.sportlottery.network.common.PagingParams
import org.cxct.sportlottery.network.common.TimeRangeParams

@KeepMembers
data class OtherBetHistoryRequest(
    override val page: Int? = null,
    override val pageSize: Int? = null,
    override val startTime: String? = null,
    override val endTime: String? = null,
    val firmType: String ?= null, //厂商类型，如AG，KT
    val firmCode: String ?= null, //厂商下的游戏
    val gameType: String ?= null, //游戏
    val code: String? = null, //游戏分类编码
) : TimeRangeParams, PagingParams
